package de.kriegel.studip.main.config

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import de.kriegel.studip.R
import de.kriegel.studip.main.MainActivity
import kotlinx.android.synthetic.main.settings_fragment.*
import timber.log.Timber

class ConfigFragment : Fragment() {

    companion object {
        fun newInstance(args: Bundle?) = ConfigFragment().apply { arguments = args }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.settings_fragment, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notificationsCoursesAnnouncementsEnabled.setOnClickListener({
            var switch = it as Switch

            Timber.i("NotificationEnabled switch clicked ${switch.isChecked}")

            if (switch.isChecked) {
                MainActivity.appConfiguration.startCourseNewsNotificationJobService(3000)
            } else {
                MainActivity.appConfiguration.stopCourseNewsNotificationJobService()
            }
        })
    }

}