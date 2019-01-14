package de.kriegel.studip.start

import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import de.kriegel.studip.BuildConfig
import de.kriegel.studip.R
import de.kriegel.studip.config.AppConfiguration
import de.kriegel.studip.login.LoginActivity
import de.kriegel.studip.main.MainActivity
import de.kriegel.studip.util.Connectivity
import kotlinx.android.synthetic.main.activity_start.*
import timber.log.Timber
import java.util.*


class StartActivity : AppCompatActivity() {

    val STARTUP_DELAY = 100L
    val ANIM_ITEM_DURATION = 1500L
    val ITEM_DELAY = 200L

    private val animationStarted = false

    val AUTO_LOGIN = true

    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startApplication()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {

        if (!hasFocus || animationStarted) {
            return
        }

        animate()

        super.onWindowFocusChanged(hasFocus)
    }

    private fun animate() {
        val logoImageView = findViewById(R.id.img_logo) as ImageView
        val container = findViewById<View>(R.id.container) as ViewGroup

        ViewCompat.animate(logoImageView)
            .translationY(-200f)
            .setStartDelay(STARTUP_DELAY)
            .setDuration(ANIM_ITEM_DURATION).setInterpolator(
                DecelerateInterpolator(1.2f)
            ).start()

        for (i in 0 until container.childCount) {
            val v = container.getChildAt(i)
            val viewAnimator: ViewPropertyAnimatorCompat

            if (v !is Button) {
                viewAnimator = ViewCompat.animate(v)
                    .translationY(50f).alpha(1f)
                    .setStartDelay(ITEM_DELAY * i + 500)
                    .setDuration(1000)
            } else {
                viewAnimator = ViewCompat.animate(v)
                    .scaleY(1f).scaleX(1f)
                    .setStartDelay(ITEM_DELAY * i + 500)
                    .setDuration(500)
            }

            viewAnimator.setInterpolator(DecelerateInterpolator()).start()
        }
    }

    private fun startApplication() {
        val startDate = Date()

        var appConfiguration = AppConfiguration(this)

        val serverUri = appConfiguration.getLoginServerFromSharedPreferences()
        val serverCredentials = appConfiguration.getLoginCredentialsFromSharedPreferences()

        if(!serverCredentials.username.isEmpty()) {
            welcomeTextView.text = "Welcome ${serverCredentials.username}"
        }

        Timber.i("Fetched server: $serverUri and user: $serverCredentials from shared preferences")

        Thread {

            if (AUTO_LOGIN && !serverUri.toString().isEmpty()) {
                Timber.i("Checking if server ${serverUri.toString()} is reachable")
                val isServerReachable = Connectivity.isURLReachable(serverUri.toURL(), this)

                Timber.d("Is server reachable? $isServerReachable")

                if (isServerReachable) {

                    val credentialsExist =
                        !serverCredentials.username.isEmpty() && !serverCredentials.password.isEmpty()
                    Timber.i("Credentials are not empty? $credentialsExist")

                    if (credentialsExist) {

                        if (appConfiguration.performLogin(serverUri, serverCredentials)) {

                            val now = Date()
                            if((now.time - startDate.time) < ANIM_ITEM_DURATION) {
                                Timber.i("Sleeping ${ANIM_ITEM_DURATION - (now.time - startDate.time)} ms")
                                Thread.sleep(ANIM_ITEM_DURATION - (now.time - startDate.time))
                            }

                            var intent = Intent(this, MainActivity::class.java)
                            intent.putExtra(getString(R.string.studip_uri_key), serverUri.toString())
                            intent.putExtra(getString(R.string.studip_username_key), serverCredentials.username)
                            intent.putExtra(getString(R.string.studip_password_key), serverCredentials.password)

                            startActivity(intent)
                            finish()
                            return@Thread
                        } else {
                            Timber.e("Could not perform login")
                        }

                    }
                }
            }

            val now = Date()
            if((now.time - startDate.time) < ANIM_ITEM_DURATION) {
                Timber.i("Sleeping ${ANIM_ITEM_DURATION - (now.time - startDate.time)} ms")
                Thread.sleep(ANIM_ITEM_DURATION - (now.time - startDate.time))
            }
            startActivity(Intent(this, LoginActivity::class.java))
            finish()

        }.start()
    }

}