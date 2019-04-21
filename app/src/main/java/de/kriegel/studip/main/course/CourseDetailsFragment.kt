package de.kriegel.studip.main.course

import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.kriegel.studip.R
import de.kriegel.studip.client.content.model.data.Course
import de.kriegel.studip.client.content.model.data.Id
import de.kriegel.studip.main.MainActivity
import kotlinx.android.synthetic.main.course_details_fragment.*
import timber.log.Timber
import java.util.*

class CourseDetailsFragment() : Fragment() {

    companion object {
        fun newInstance(args: Bundle, course: Course) = CourseDetailsFragment().apply {
            arguments = args
            this.course = course
        }
    }

    private lateinit var course: Course

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            loadFromBundle(savedInstanceState)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.course_details_fragment, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val courseDetailPanels = buildCourseDetailPanels()
        courseDetailPanels.forEach {
            linearLayout.addView(it)
        }
    }

    fun buildCourseDetailPanels(): List<CourseDetailsPanel> {

        val courseDetailPanels = mutableListOf<CourseDetailsPanel>()

        // general information
        val generalContentList = mutableListOf<Pair<String, Any>>(
            Pair("Title", course.title),
            Pair("Subtitle", course.subtitle),
            Pair(
                "Semester",
                MainActivity.appConfiguration.client.courseService.getSemesterById(course.startSemesterId).title
            ),
            Pair("Participants", course.memberCounts.entries.sumBy { it.value })
        )


        courseDetailPanels.add(CourseDetailsPanel(context!!, "General Information", generalContentList))

        // lecturer
        val lecturerContentList = mutableListOf<Pair<String, Any>>()
        course.lecturers.forEach {
            val userInformation = it.userInformation

            var lecturerDescription = "${userInformation.formatted}"
            if (!it.email.isBlank()) lecturerDescription += " (${it.email})"
            lecturerContentList.add(Pair("empty", lecturerDescription))
        }

        courseDetailPanels.add(CourseDetailsPanel(context!!, "Lecturer", lecturerContentList))

        // comment / description
        val commentDescriptionList = mutableListOf<Pair<String, Any>>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            commentDescriptionList.add(
                Pair(
                    "empty",
                    Html.fromHtml(course.description, Html.FROM_HTML_MODE_COMPACT)
                )
            )
        } else {
            commentDescriptionList.add(Pair("empty", Html.fromHtml(course.description)))
        }

        courseDetailPanels.add(CourseDetailsPanel(context!!, "Comment / Description", commentDescriptionList))

        // participants info
        val participantsInfoList = mutableListOf<Pair<String, Any>>()

        course.memberCounts.forEach { type, count ->
            participantsInfoList.add(Pair(type.getName().capitalize(), count))
        }

        courseDetailPanels.add(CourseDetailsPanel(context!!, "Members", participantsInfoList))

        return courseDetailPanels
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        course.run {
            outState.putSerializable("course", course)
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

        (bundle.getSerializable("course") as Course).run {
            course = this
        }
    }
}