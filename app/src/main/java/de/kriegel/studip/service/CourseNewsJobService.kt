package de.kriegel.studip.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.AppCompatActivity
import de.kriegel.studip.R
import timber.log.Timber
import android.app.PendingIntent
import android.content.Intent
import android.os.PersistableBundle
import de.kriegel.studip.client.auth.Credentials
import de.kriegel.studip.client.service.StudIPClient
import de.kriegel.studip.config.AppConfiguration
import de.kriegel.studip.config.Constants
import de.kriegel.studip.start.StartActivity
import java.lang.Exception
import java.net.URI
import java.util.*


class CourseNewsJobService : JobService() {

    lateinit var studIPClient: StudIPClient
    lateinit var appConfiguration: AppConfiguration
    lateinit var courseNewsManager: CourseNewsManager

    lateinit var courseNewsNotificationChannel: NotificationChannel

    override fun onStartJob(p0: JobParameters?): Boolean {
        Timber.i("Running CourseNewsJobService")

        appConfiguration = AppConfiguration(baseContext)
        val serverUri = appConfiguration.getLoginServerFromSharedPreferences()
        val serverCredentials = appConfiguration.getLoginCredentialsFromSharedPreferences()

        initClient(serverUri, serverCredentials)
        initNotificationChannel()

        val downloadDirectory = appConfiguration.getDefaultDownloadLocation()
        if (downloadDirectory != null) {
            Timber.i("Setting download directory of studIPClient from ${studIPClient.courseService.downloadManager.downloadDirectory} to $downloadDirectory")
            studIPClient.courseService.downloadManager.downloadDirectory = downloadDirectory
        } else {
            Timber.e("Could not set download directory")
        }

        // logs dir is mandatory to get new course news
        val logsDirectory = appConfiguration.getDefaultLogsLocation()
        if (logsDirectory != null) {
            courseNewsManager = CourseNewsManager(studIPClient, logsDirectory, baseContext)
        } else {
            jobFinished(p0, false)
        }

        var runSuccessful = true

        try {
            createNotificationsForNewCourseNews()
        } catch (e: Exception) {
            runSuccessful = false
            e.printStackTrace()
        }

        //Reschedule the Service before calling job finished
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            rescheduleSelf(p0);

        jobFinished(p0, false)

        // false => no additional threads started
        // true => additional threads are running
        return false
    }

    override fun onStopJob(p0: JobParameters?): Boolean {
        Timber.i("Stopping ${CourseNewsJobService::class.simpleName}")

        studIPClient?.shutdown()

        return false
    }

    private fun createNotificationsForNewCourseNews() {
        courseNewsManager.getNewCourseNews().forEach { course, courseNewsList ->
            run {

                courseNewsList.forEach { courseNews ->
                    run {
                        val notificationId = Constants.calculateIdFromString(courseNews.topic + Date().time)

                        var notificationCompatBuilder =
                            NotificationCompat.Builder(this, courseNewsNotificationChannel.id)
                                .setContentTitle(course.title)
                                .setContentText(courseNews.topic)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setSmallIcon(R.drawable.ic_studip_logo)

                        Timber.i("Creating notification with id $notificationId")

                        val intent = Intent(this, StartActivity::class.java)
                        intent.putExtra("NOTIFICATION_ID", courseNews.id.asHex())

                        val contentIntent = PendingIntent.getActivity(
                            this,
                            0,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )

                        notificationCompatBuilder.setContentIntent(contentIntent)

                        with(NotificationManagerCompat.from(this)) {
                            // notificationId is a unique int for each notification that you must define
                            notify(notificationId, notificationCompatBuilder.build())
                        }
                    }
                }
            }
        }
    }

    private fun initClient(serverUri: URI, serverCredentials: Credentials) {

        studIPClient = StudIPClient(serverUri, serverCredentials)
        studIPClient.authService.authenticate()

    }

    private fun initNotificationChannel() {

        val notificationManager = getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager

        // init the notification channel if not existing yet
        // create own channel or use message channel otherwise
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Create the NotificationChannel
            val id = getString(R.string.notification_studip_course_news_channel_name)
            val name = getString(R.string.notification_studip_course_news_channel_name)
            val descriptionText = getString(R.string.notification_studip_course_news_channel_description)

            val importance = NotificationManager.IMPORTANCE_DEFAULT
            courseNewsNotificationChannel = NotificationChannel(id, name, importance)
            courseNewsNotificationChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(courseNewsNotificationChannel)
        } else {
            courseNewsNotificationChannel =
                    notificationManager.getNotificationChannel(NotificationCompat.CATEGORY_MESSAGE)
        }

    }

    fun rescheduleSelf(p0: JobParameters?) {

        Timber.i("Rescheduling self")

        val jobScheduler = applicationContext.getSystemService(JOB_SCHEDULER_SERVICE);
        val jobBuilder = JobInfo.Builder(
            p0!!.jobId,
            ComponentName(applicationContext, CourseNewsJobService::class.java)
        );

        val pBundle = PersistableBundle()

        /*
        pBundle.putInt(
            baseContext.resources.getString(R.string.current_notification_id_counter_key),
            currentNotificationIdCounter
        )*/
        jobBuilder.setExtras(pBundle)

        Timber.i("Scheduling CourseNewsNotificationJobService for ${(20 * 1000) / 60000.0} minutes")

        /* For Android N and Upper Versions */
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            jobBuilder
                .setMinimumLatency(20 * 1000) //YOUR_TIME_INTERVAL
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            val result = (jobScheduler as JobScheduler).schedule(jobBuilder.build())
            if (JobScheduler.RESULT_FAILURE == result) {
                Timber.e("Could not reschedule the CourseNewsJobService")
            }
        }

    }
}