package de.kriegel.studip.login

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import de.kriegel.studip.BuildConfig
import de.kriegel.studip.R
import de.kriegel.studip.client.auth.Credentials
import de.kriegel.studip.client.service.StudIPClient
import de.kriegel.studip.config.AppConfiguration
import de.kriegel.studip.main.MainActivity
import de.kriegel.studip.start.StartActivity
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber
import java.net.URI
import java.util.*
import java.util.function.Predicate

class LoginActivity : AppCompatActivity() {

    lateinit var client: StudIPClient

    lateinit var serverService: ServerService
    lateinit var appConfiguration: AppConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        appConfiguration = AppConfiguration(this)

        serverService = ServerService(baseContext)
        var allServers = serverService.allServers

        var serverPairSpinnerAdapter = ServerPairSpinnerAdapter(baseContext, allServers)

        serverSpinner.adapter = serverPairSpinnerAdapter

        val serverUri = appConfiguration.getLoginServerFromSharedPreferences()
        val serverCredentials = appConfiguration.getLoginCredentialsFromSharedPreferences()

        Timber.i("From SharedPreferences:")
        Timber.i("Server: ${serverUri.toString()}")

        var optional = allServers.stream().filter(Predicate {
            it.serverPair.second.toString().equals(
                serverUri.toString()
            )
        }).findAny()

        if (optional.isPresent) {
            serverSpinner.setSelection(allServers.indexOf(optional.get()))
        }

        usernameEditText.setText(serverCredentials.username)
        passwordEditText.setText(serverCredentials.password)

        /*
        if (!NO_AUTO_LOGIN && !serverUri.toString().isEmpty() && !serverCredentials.username.isEmpty() && !serverCredentials.password.isEmpty()) {
            Timber.i("Logging in from shared preferences")
            performLogin()
        }
        */

    }

    fun performLogin(view: View) {
        Timber.i("Performing login")

        var serverUri: URI? = null

        if (serverSpinner.selectedItem is CustomServerPairWrapper) {
            serverUri = (serverSpinner.selectedItem as CustomServerPairWrapper).serverPair.second
        }

        Timber.i("UI: Server: ${serverSpinner.selectedItem} $serverUri")
        Timber.i("UI: User: ${usernameEditText.text} ${passwordEditText.text.replace(Regex("."), "*")}")

        if (serverUri == null || usernameEditText.text.isEmpty() || passwordEditText.text.isEmpty()) {
            Toast.makeText(this, "Missing data", LENGTH_SHORT).show()
        } else {

            var serverCredentials = Credentials(usernameEditText.text.toString(), passwordEditText.text.toString())

            if(appConfiguration.performLogin(serverUri, serverCredentials)) {
                var intent = Intent(this, MainActivity::class.java)
                intent.putExtra(getString(R.string.studip_uri_key), serverUri.toString())
                intent.putExtra(getString(R.string.studip_username_key), serverCredentials.username)
                intent.putExtra(getString(R.string.studip_password_key), serverCredentials.password)

                startActivity(intent)
                finish()
            } else {
                Timber.e("Could not perform login")
            }
        }

    }

}
