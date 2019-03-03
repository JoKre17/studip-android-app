package de.kriegel.studip.main.course

import android.content.res.Resources
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import de.kriegel.studip.R
import de.kriegel.studip.client.content.model.data.Course
import de.kriegel.studip.client.content.model.data.Id
import de.kriegel.studip.main.MainActivity
import kotlinx.android.synthetic.main.course_fragment.*
import timber.log.Timber
import java.io.Serializable
import kotlin.coroutines.coroutineContext

class CourseFragment() : Fragment() {

    companion object {
        fun newInstance(args: Bundle, course: Course) = CourseFragment().apply {
            arguments = args
            this.course = course
        }
    }

    private lateinit var pagerAdapter: PagerAdapter
    private lateinit var course: Course

    private lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            loadFromBundle(savedInstanceState)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.course_fragment, container, false)

        pagerAdapter = PagerAdapter(resources, fragmentManager!!, course)

        viewPager = view.findViewById(R.id.viewPager)
        viewPager.adapter = pagerAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        course?.run {
            outState.putSerializable("courseId", course.id)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (savedInstanceState != null) {
            loadFromBundle(savedInstanceState)
        }
    }

    private fun loadFromBundle(bundle: Bundle) {
        Timber.d("loadFromBundle")

        (bundle.getSerializable("courseId") as Id)?.run {
            course = MainActivity.appConfiguration.client.courseService.getCourseById(this)
        }
    }

    class PagerAdapter(res: Resources, fm: FragmentManager, course: Course) : FragmentStatePagerAdapter(fm) {

        private val fragments: List<Fragment>
        private val tabTitles: List<String>

        init {
            val courseDetailsFragment = CourseDetailsFragment.newInstance(args = Bundle(), course = course)
            val courseForumFragment = CourseForumFragment.newInstance(args = Bundle(), course = course)
            val courseParticipantsFragment = CourseParticipantsFragment.newInstance(args = Bundle(), course = course)
            val courseFilesFragment = CourseFilesFragment.newInstance(args = Bundle(), course = course)
            val courseAnnouncementsFragment = CourseAnnouncementsFragment.newInstance(args = Bundle(), course = course)

            fragments = listOf(
                courseDetailsFragment,
                courseForumFragment,
                courseParticipantsFragment,
                courseFilesFragment,
                courseAnnouncementsFragment
            )

            tabTitles = listOf(
                res.getString(R.string.course_details),
                res.getString(R.string.course_forum),
                res.getString(R.string.course_participants),
                res.getString(R.string.course_files),
                res.getString(R.string.course_announcements)
            )
        }

        override fun getCount(): Int = fragments.size

        override fun getItem(i: Int): Fragment = fragments.get(i)

        override fun getPageTitle(position: Int): CharSequence? {
            return tabTitles.get(position)
        }
    }
}