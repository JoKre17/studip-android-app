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
        fun newInstance(args: Bundle, course: Course, initialShownTabIndex : Int) = CourseFragment().apply {
            arguments = args
            this.course = course
            this.initialShownTabIndex = initialShownTabIndex
        }
    }


    private lateinit var pagerAdapter: PagerAdapter
    private lateinit var course: Course
    private var initialShownTabIndex : Int = 0

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

        tabLayout.getTabAt(initialShownTabIndex)?.select()

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

    class PagerAdapter(res: Resources, fm : FragmentManager, val course: Course) : FragmentStatePagerAdapter(fm) {

        private val tabTitles: List<String>

        private val fragmentsMap = HashMap<String, Fragment>()

        init {

            tabTitles = listOf(
                res.getString(R.string.course_details),
                res.getString(R.string.course_forum),
                res.getString(R.string.course_participants),
                res.getString(R.string.course_files),
                res.getString(R.string.course_announcements)
            )

            Timber.d("tabTitles: ${tabTitles.toString()}")
        }

        override fun getCount(): Int = tabTitles.size

        override fun getItem(i: Int): Fragment {

            if(!fragmentsMap.containsKey(tabTitles.get(i))) {

                // The fragments are created if they are needed. The PagerAdapter calls also neighbour fragments of the tab layout via getItem when one is displayed
                when(tabTitles.get(i)) {
                    tabTitles.get(0) -> fragmentsMap.put(tabTitles.get(i), CourseDetailsFragment.newInstance(args = Bundle(), course = course))
                    tabTitles.get(1) -> fragmentsMap.put(tabTitles.get(i), CourseForumFragment.newInstance(args = Bundle(), course = course))
                    tabTitles.get(2) -> fragmentsMap.put(tabTitles.get(i), CourseParticipantsFragment.newInstance(args = Bundle(), course = course))
                    tabTitles.get(3) -> fragmentsMap.put(tabTitles.get(i), CourseFilesFragment.newInstance(args = Bundle(), course = course))
                    tabTitles.get(4) -> fragmentsMap.put(tabTitles.get(i), CourseAnnouncementsFragment.newInstance(args = Bundle(), course = course))
                }
            }

            return fragmentsMap.get(tabTitles.get(i))!!
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return tabTitles.get(position)
        }
    }
}