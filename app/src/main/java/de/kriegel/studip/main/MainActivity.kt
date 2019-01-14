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


class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var client: StudIPClient
        lateinit var appConfiguration: AppConfiguration
    }

    private lateinit var mDrawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

            var fragment : Fragment? = null

            when(menuItem.itemId) {
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
        client?.shutdown()
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



}
