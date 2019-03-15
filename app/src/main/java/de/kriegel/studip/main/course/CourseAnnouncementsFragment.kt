package de.kriegel.studip.main.course

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.kriegel.studip.R
import de.kriegel.studip.client.content.model.data.Course
import timber.log.Timber

class CourseAnnouncementsFragment() : Fragment() {

    companion object {
        fun newInstance(args: Bundle, course: Course) = CourseAnnouncementsFragment().apply {
            arguments = args
            this.course = course
        }
    }

    private lateinit var course: Course

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.course_announcements_fragment, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    private fun loadFromBundle(bundle: Bundle) {
        Timber.d("loadFromBundle")

    }

}