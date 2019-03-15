package de.kriegel.studip.config

import android.content.Context
import android.content.Intent
import de.kriegel.studip.R
import de.kriegel.studip.client.auth.Credentials
import de.kriegel.studip.client.service.StudIPClient
import de.kriegel.studip.main.MainActivity
import de.kriegel.studip.service.CourseNewsJobScheduler
import timber.log.Timber
import java.io.File
import java.net.URI
import java.nio.file.Path

class AppConfiguration(val context: Context) {

    private val LOGS_DIR_NAME = "logs"
    private val DOWNLOAD_DIR_NAME = "downloads"

    lateinit var client : StudIPClient

    private var isDownloadEnabled = false
    private var isNotificationEnabled = false

    private var courseNewsJobScheduler: CourseNewsJobScheduler

    init {
        courseNewsJobScheduler = CourseNewsJobScheduler()
    }

    /*
    fun setClient(client: StudIPClient) {
        this.client = client
    }
    */

    fun logAllSharedPreferences() {
        val prefs = context.getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)
        Timber.i("All shared preferences:")
        prefs.all.entries.forEach {
            Timber.i("${it.key} : ${it.value}")
        }
    }

    fun getLoginCredentialsFromSharedPreferences(): Credentials {
        val prefs = context.getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)

        val username = prefs.getString(context.getString(R.string.studip_username_key), "")
        val password = prefs.getString(context.getString(R.string.studip_password_key), "")

        val serverCredentials = Credentials(username, password)

        return serverCredentials
    }

    fun getLoginServerFromSharedPreferences(): URI {
        val prefs = context.getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)

        Timber.d("StudIP url in prefs: $prefs.getString(context.getString(R.string.studip_uri_key)")
        val serverUri = URI(prefs.getString(context.getString(R.string.studip_uri_key), ""))

        return serverUri
    }

    fun getDefaultLogsLocation(): File? {

        val absoluteLogsLocation = context.filesDir.absolutePath + File.separator + LOGS_DIR_NAME
        val logsDir = File(absoluteLogsLocation)
        if (!logsDir.exists()) {
            val success = logsDir.mkdirs()

            when (success) {
                true -> return logsDir
                false -> {
                    Timber.e("Could not create directory ${logsDir.absolutePath}")
                    return null
                }
            }
        }

        return logsDir
    }

    fun getDefaultDownloadLocation(): File? {

        val absoluteLogsLocation = context.filesDir.absolutePath + File.separator + DOWNLOAD_DIR_NAME
        val logsDir = File(absoluteLogsLocation)
        if (!logsDir.exists()) {
            val success = logsDir.mkdirs()

            when (success) {
                true -> return logsDir
                false -> {
                    Timber.e("Could not create directory ${logsDir.absolutePath}")
                    return null
                }
            }
        }

        return logsDir
    }

    fun startCourseNewsNotificationJobService(intervalMillis: Long) {
        Timber.i("Starting CourseNewsNotificationJob")

        // if there is already a job scheduled, remove it first
        if (courseNewsJobScheduler.isCourseNewsNotificationJobAlreadyScheduled(context)) {
            val jobId = courseNewsJobScheduler.getCourseNewsNotificationJobId(context)
            courseNewsJobScheduler.unscheduleCourseNewsNotificationJob(context, jobId)
        }

        courseNewsJobScheduler.scheduleCourseNewsNotificationJob(
            context,
            intervalMillis,
            getLoginServerFromSharedPreferences(),
            getLoginCredentialsFromSharedPreferences()
        )
    }

    fun stopCourseNewsNotificationJobService() {
        Timber.i("Stopping CourseNewsNotificationJob")

        // if there is already a job scheduled, remove it
        if (courseNewsJobScheduler.isCourseNewsNotificationJobAlreadyScheduled(context)) {
            val jobId = courseNewsJobScheduler.getCourseNewsNotificationJobId(context)
            courseNewsJobScheduler.unscheduleCourseNewsNotificationJob(context, jobId)
        }
    }

    fun performLogin(serverUri: URI, serverCredentials: Credentials): Boolean {
        client = StudIPClient(serverUri, serverCredentials)
        client.courseService.downloadManager.downloadDirectory = getDefaultDownloadLocation()

        Timber.i("Authenticating...")
        var isAuthenticated = false
        //retries
        repeat(5) {
            isAuthenticated = client.authService.authenticate()
            if (isAuthenticated) {
                return@repeat
            } else {
                Timber.w("Login failed (${it}. Try)")
                Thread.sleep(500)
            }
        }
        Timber.i("Is authenticated? $isAuthenticated")

        if (isAuthenticated) {
            // store to shared preferences
            val prefs = context.getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)
            with(prefs.edit()) {
                putString(context.getString(de.kriegel.studip.R.string.studip_uri_key), serverUri.toString())
                putString(context.getString(de.kriegel.studip.R.string.studip_username_key), serverCredentials.username)
                putString(context.getString(de.kriegel.studip.R.string.studip_password_key), serverCredentials.password)

                commit()
            }
        }

        return isAuthenticated
    }

}