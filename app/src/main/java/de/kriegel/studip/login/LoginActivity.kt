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
import de.kriegel.studip.main.MainActivity
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber
import java.net.URI

class LoginActivity : AppCompatActivity() {

    lateinit var client: StudIPClient

    val SHARED_PREFERENCES_FILE: String = "de.kriegel.studip.prefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.studip_server_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            serverSpinner.adapter = adapter
        }

        // read from shared preferences
        val prefs = this.getPreferences(Context.MODE_PRIVATE)
        Timber.i("All shared preferences:")
        prefs.all.entries.forEach {
            Timber.i("${it.key} : ${it.value}")
        }

        val serverUri = URI(prefs.getString(getString(R.string.studip_uri_key), ""))
        val serverCredentials = Credentials(
            prefs.getString(getString(R.string.studip_username_key), ""),
            prefs.getString(getString(R.string.studip_password_key), "")
        )

        Timber.i("From SharedPreferences:")
        Timber.i("Server: ${serverUri.toString()}")
        Timber.i("User  : ${serverCredentials.username} ${serverCredentials.password}")

        usernameEditText.setText(serverCredentials.username)
        passwordEditText.setText(serverCredentials.password)

        if (!serverUri.toString().isEmpty() && !serverCredentials.username.isEmpty() && !serverCredentials.password.isEmpty()) {
            Timber.i("Logging in from shared preferences")
            performLogin()
        }

    }

    fun performLogin(view: View) {
        performLogin()
    }

    fun performLogin() {
        Timber.i("Performing login")

        Timber.i("UI: Server: ${serverSpinner.selectedItem.toString()}")
        Timber.i("UI: User: ${usernameEditText.text} ${passwordEditText.text}")

        if (serverSpinner.selectedItem == null || usernameEditText.text.isEmpty() || passwordEditText.text.isEmpty()) {
            Toast.makeText(this, "Missing data", LENGTH_SHORT).show()
        } else {

            var serverUri = URI("https://studip.uni-hannover.de")
            var serverCredentials = Credentials(usernameEditText.text.toString(), passwordEditText.text.toString())

            client = StudIPClient(serverUri, serverCredentials)

            Timber.i("Authenticating...")
            var isAuthenticated = client.authService.authenticate()
            Timber.i("Is authenticated? $isAuthenticated")

            textView.setText(client.authService.currentUserId.toString())

            if (isAuthenticated) {
                Timber.d("Start main activity")

                client.shutdown()

                // store to shared preferences
                val prefs = this.getPreferences(Context.MODE_PRIVATE)
                with(prefs.edit()) {
                    putString(getString(R.string.studip_uri_key), serverUri.toString())
                    putString(getString(R.string.studip_username_key), serverCredentials.username)
                    putString(getString(R.string.studip_password_key), serverCredentials.password)

                    commit()
                }

                var intent = Intent(this, MainActivity::class.java)
                intent.putExtra(getString(R.string.studip_uri_key), serverUri.toString())
                intent.putExtra(getString(R.string.studip_username_key), serverCredentials.username)
                intent.putExtra(getString(R.string.studip_password_key), serverCredentials.password)
                this.finish()
                startActivity(intent)
            }
        }

    }
}
