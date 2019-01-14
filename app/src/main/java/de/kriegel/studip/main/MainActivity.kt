package de.kriegel.studip.main

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import de.kriegel.studip.R
import de.kriegel.studip.client.auth.Credentials
import de.kriegel.studip.client.service.StudIPClient
import de.kriegel.studip.config.AppConfiguration
import de.kriegel.studip.main.config.ConfigFragment
import de.kriegel.studip.main.course.CourseFragment
import timber.log.Timber
import java.net.URI
import android.widget.Toast
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var client: StudIPClient
        lateinit var appConfiguration: AppConfiguration
    }

    private lateinit var mDrawerLayout: DrawerLayout
    private var appCloseWarningOccurance: Date? = null
    lateinit private var appCloseWarningToast: Toast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        appCloseWarningToast = Toast.makeText(this, R.string.app_close_warning, Toast.LENGTH_SHORT)

        initClient()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }

        mDrawerLayout = findViewById(R.id.drawer_layout)

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            // set item as selected to persist highlight
            menuItem.isChecked = true
            // close drawer when item is tapped
            mDrawerLayout.closeDrawers()

            // Add code here to update the UI based on the item selected
            // For example, swap UI fragments here

            Timber.i("Clicked $menuItem")

            var fragment: Fragment? = null

            when (menuItem.itemId) {
                R.id.nav_courses -> fragment = CourseFragment()
                R.id.nav_settings -> fragment = ConfigFragment()
                R.id.nav_about -> fragment = AboutFragment()
            }

            //replacing the fragment
            if (fragment != null) {
                val ft = supportFragmentManager.beginTransaction()
                ft.replace(R.id.content_frame, fragment)
                ft.commit()
            }

            mDrawerLayout.closeDrawer(GravityCompat.START)

            true
        }

        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.content_frame, LandingFragment())
        ft.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        client.shutdown()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                mDrawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initClient() {
        appConfiguration = AppConfiguration(this)

        var serverUri = URI(intent.getStringExtra(getString(R.string.studip_uri_key)))
        var serverCredentials = Credentials(
            intent.getStringExtra(getString(R.string.studip_username_key)),
            intent.getStringExtra(getString(R.string.studip_password_key))
        )

        Timber.i("Server: ${serverUri.path}")
        Timber.i("User  : $serverCredentials")

        client = StudIPClient(serverUri, serverCredentials)
    }

    override fun onBackPressed() {

        // if back was pressed once while the viewPager is on page 0
        if (appCloseWarningOccurance != null) {
            // if the last time the warning was shown was less than 2 seconds ago
            if (Date().getTime() - appCloseWarningOccurance!!.getTime() < 2000) {
                appCloseWarningToast.cancel()
                super.onBackPressed()
            }
        }

        appCloseWarningOccurance = Date()
        appCloseWarningToast!!.show()
    }

}
