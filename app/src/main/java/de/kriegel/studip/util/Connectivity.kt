package de.kriegel.studip.util

import android.content.Context
import android.net.NetworkInfo
import android.content.Context.CONNECTIVITY_SERVICE
import android.support.v4.content.ContextCompat.getSystemService
import android.net.ConnectivityManager
import timber.log.Timber
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL


class Connectivity {

    companion object {

        val CONNECTIOM_TIMEOUT_MILLIS = 10 * 1000 // 10 seconds

        fun isURLReachable(url: URL, context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo
            if (netInfo != null && netInfo.isConnected) {
                try {
                    val urlc = url.openConnection() as HttpURLConnection
                    urlc.setConnectTimeout(CONNECTIOM_TIMEOUT_MILLIS)
                    urlc.connect()
                    if (urlc.getResponseCode() === 200) {        // 200 = "OK" code (http connection is fine).
                        return true
                    } else {
                        return false
                    }
                } catch (e1: MalformedURLException) {
                    return false
                } catch (e: IOException) {
                    return false
                }

            }
            return false
        }

    }

}