package de.kriegel.studip.config

import android.provider.SyncStateContract.Helpers.update
import timber.log.Timber
import java.security.NoSuchAlgorithmException
import kotlin.experimental.and


object Constants {

    // Application
    const val SHARED_PREFERENCES_FILE = "de.kriegel.studip.prefs"
    const val PACKAGE_NAME = "de.kriegel.studip"

    // Notification
    const val SERVICE_COURSE_NEWS_JOB_ID_KEY = "COURSE_NEWS_JOB_ID"
    var SERVICE_COURSE_NEWS_JOB_ID: Int

    init {
        Timber.i("SERVICE_COURSE_NEWS_JOB_ID of $PACKAGE_NAME is ${calculateIdFromString(PACKAGE_NAME)}")
        SERVICE_COURSE_NEWS_JOB_ID = calculateIdFromString(PACKAGE_NAME)
    }

    fun calculateIdFromString(s: String): Int {

        var messageDigest = md5(s)

        when ((messageDigest.size >= 4)) {
            true -> {
                val i = 0xFF and messageDigest[messageDigest.size - 1].toInt() shl 24 or (0xFF and messageDigest[messageDigest.size - 2].toInt() shl 16) or
                        (0xFF and messageDigest[messageDigest.size - 3].toInt() shl 8) or (0xFF and messageDigest[messageDigest.size - 4].toInt())

                return i
            }
            else -> {
                Timber.e("digested message does not exceed length >= 4")
                return -1
            }
        }

    }

    fun md5(s: String): ByteArray {
        try {
            // Create MD5 Hash
            val digest = java.security.MessageDigest.getInstance("MD5")
            digest.update(s.toByteArray())
            val messageDigest = digest.digest()


            return messageDigest;
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        return ByteArray(0)
    }

}