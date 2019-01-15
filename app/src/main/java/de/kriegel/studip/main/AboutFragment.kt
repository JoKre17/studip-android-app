package de.kriegel.studip.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.kriegel.studip.R
import de.kriegel.studip.main.course.CourseFragment

class AboutFragment : Fragment() {

    companion object {
        fun newInstance(args: Bundle) = AboutFragment().apply { arguments = args }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.about_fragment, container, false)
    }

}