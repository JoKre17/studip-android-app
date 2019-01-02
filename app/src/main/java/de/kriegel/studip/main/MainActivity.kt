package de.kriegel.studip.main

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import de.kriegel.studip.BuildConfig
import de.kriegel.studip.R
import de.kriegel.studip.client.auth.Credentials
import de.kriegel.studip.client.service.StudIPClient
import de.kriegel.studip.login.LoginActivity
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber
import java.net.URI

class MainActivity : AppCompatActivity() {

    lateinit var client: StudIPClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var serverUri = URI(intent.getStringExtra(getString(R.string.studip_uri_key)))
        var serverCredentials = Credentials(
            intent.getStringExtra(getString(R.string.studip_username_key)),
            intent.getStringExtra(getString(R.string.studip_password_key))
        )

        Timber.i("Server: ${serverUri.path}")
        Timber.i("User  : ${serverCredentials.username} ${serverCredentials.password}")

        client = StudIPClient(serverUri, serverCredentials)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

    }

    override fun onDestroy() {
        super.onDestroy()
        client.shutdown()
    }

}
