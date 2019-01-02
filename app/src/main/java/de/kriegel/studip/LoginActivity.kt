package de.kriegel.studip

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import de.kriegel.studip.client.auth.Credentials
import de.kriegel.studip.client.service.StudIPClient
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber
import java.net.URI

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        var client = StudIPClient(URI("https://studip.uni-hannover.de"), Credentials("JK_14", "Aiedail95"))

        Timber.i("before authentication")
        var authenticated = client.authService.authenticate();

        Timber.i("is authenticated: " + authenticated)

        textView.setText(client.authService.currentUserId.toString())

    }
}
