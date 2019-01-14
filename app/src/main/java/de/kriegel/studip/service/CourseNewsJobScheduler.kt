package de.kriegel.studip.service

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.app.job.JobScheduler.RESULT_FAILURE
import android.app.job.JobScheduler.RESULT_SUCCESS
import android.content.ComponentName
import android.content.Context
import timber.log.Timber
import android.os.Build
import android.os.PersistableBundle
import de.kriegel.studip.R
import de.kriegel.studip.client.auth.Credentials
import de.kriegel.studip.config.Constants
import java.net.URI


class CourseNewsJobScheduler {

    init {

    }

    fun isCourseNewsNotificationJobAlreadyScheduled(context: Context): Boolean {

        var sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)

        var courseNewsJobId = sharedPreferences.getInt(Constants.SERVICE_COURSE_NEWS_JOB_ID_KEY, -1)

        if (courseNewsJobId >= 0) {

            Timber.i("Checking if job with id $courseNewsJobId exists")

            val jobScheduler = context.getSystemService(JobScheduler::class.java)
            val isJobScheduled = jobScheduler!!.getPendingJob(courseNewsJobId) != null

            if (isJobScheduled) {
                return true
            } else {
                Timber.i("Removing entry '$Constants.SERVICE_COURSE_NEWS_JOB_ID_KEY' from shared preferences since no job with id $courseNewsJobId is running")
                sharedPreferences.edit().remove(Constants.SERVICE_COURSE_NEWS_JOB_ID_KEY).apply()
                return false
            }
        } else {
            Timber.i("There seems to be no service scheduled")
            return false
        }
    }

    fun getCourseNewsNotificationJobId(context: Context): Int {
        var sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)

        return sharedPreferences.getInt(Constants.SERVICE_COURSE_NEWS_JOB_ID_KEY, -1)
    }

    fun scheduleCourseNewsNotificationJob(
        context: Context,
        intervalMillis: Long,
        loginUri: URI,
        loginCredentials: Credentials
    ): Int {
        val serviceComponent = ComponentName(context, CourseNewsJobService::class.java)

        var finalIntervalMillis = if (intervalMillis < JobInfo.getMinPeriodMillis()) JobInfo.getMinPeriodMillis() else intervalMillis

        Timber.i("Scheduling CourseNewsNotificationJobService periodically every ${intervalMillis / 1000.0} seconds")

        val pBundle = PersistableBundle()

        // set login data to enable CourseNewsJobService to login and fetch notifications
        pBundle.putString(context.getString(R.string.studip_uri_key), loginUri.toString())
        pBundle.putString(context.getString(R.string.studip_username_key), loginCredentials.username)
        pBundle.putString(context.getString(R.string.studip_password_key), loginCredentials.password)

        val jobInfoBuilder = JobInfo.Builder(Constants.SERVICE_COURSE_NEWS_JOB_ID, serviceComponent)

        // fix to have service running every less than 15 minutes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            jobInfoBuilder.setMinimumLatency(intervalMillis)
        } else {
            jobInfoBuilder.setPeriodic(intervalMillis)
        }

        jobInfoBuilder.setExtras(pBundle)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NOT_ROAMING)
            .setRequiresCharging(false)
            .setPersisted(true)

        val jobInfo = jobInfoBuilder.build()

        val jobScheduler = context.getSystemService(JobScheduler::class.java)
        val result = jobScheduler.schedule(jobInfo)

        when(result) {
            RESULT_SUCCESS -> {
                Timber.i("Created CourseNewsNotificationJob with id ${Constants.SERVICE_COURSE_NEWS_JOB_ID}")

                // store the jobId in shared prefs
                var sharedPreferences =
                    context.getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)
                sharedPreferences.edit().putInt(
                    Constants.SERVICE_COURSE_NEWS_JOB_ID_KEY,
                    Constants.SERVICE_COURSE_NEWS_JOB_ID
                ).apply()

                return Constants.SERVICE_COURSE_NEWS_JOB_ID
            }
            RESULT_FAILURE -> {
                Timber.i("Could not create CourseNewsNotificationJob with id ${Constants.SERVICE_COURSE_NEWS_JOB_ID}")
            }
            else -> {
                Timber.wtf("Scheduling of jobInfo returned $result which is neither $RESULT_SUCCESS (success) nor $RESULT_FAILURE (failure)")
            }
        }

        return -1;

    }

    fun unscheduleCourseNewsNotificationJob(context: Context, jobId: Int) {

        Timber.i("Unscheduling job $jobId")

        val jobScheduler = context.getSystemService(JobScheduler::class.java)

        jobScheduler!!.cancel(jobId)

        var sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)
        sharedPreferences.edit().remove(Constants.SERVICE_COURSE_NEWS_JOB_ID_KEY).apply()
    }


}