package de.kriegel.studip.main.course

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import de.kriegel.studip.R
import de.kriegel.studip.client.content.model.data.Course
import de.kriegel.studip.client.content.model.data.Id
import de.kriegel.studip.main.MainActivity
import kotlinx.android.synthetic.main.course_list_fragment.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.Serializable
import kotlin.collections.ArrayList


class CourseListFragment() : Fragment() {

    companion object {
        fun newInstance(args: Bundle) = CourseListFragment().apply {
            arguments = args
        }
    }

    private var courses: List<Course>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            loadFromBundle(savedInstanceState)
        } else {
            if(courses == null) {
                val client = MainActivity.appConfiguration.client

                (activity as CoroutineScope).launch(Dispatchers.Default) {
                    val currentSemester = client.courseService.currentSemester
                    val currentCourses = client.courseService.getCoursesForSemesterId(currentSemester.id)
                    courses = currentCourses

                    Timber.d("Got ${currentCourses.size} courses")

                    (activity as CoroutineScope).launch(Dispatchers.Main) {
                        (recyclerView.adapter as RecyclerAdapter).update(currentCourses)
                        loadingPanel.visibility = View.GONE
                    }
                }
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.course_list_fragment, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var coursesImmutable: List<Course> = ArrayList()
        if(courses != null) {
            coursesImmutable = courses!!
            loadingPanel.visibility = View.GONE
        }

        val viewManager = LinearLayoutManager(context)
        val viewAdapter = RecyclerAdapter(coursesImmutable, fragmentManager!!)

        recyclerView.apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        courses?.run {
            var courseIds = ArrayList<Id>().toMutableList()
            forEach { courseIds.add(it.id) }

            outState.putSerializable("courseIds", courseIds as Serializable)
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

        (bundle.getSerializable("courseIds") as List<Id>)?.run {
            val coursesMutable = ArrayList<Course>().toMutableList()
            forEach {
                Timber.i("Loading course $it")
                coursesMutable.add(MainActivity.appConfiguration.client.courseService.getCourseById(it))
            }
            courses = coursesMutable.toList()
        }

    }

}

class RecyclerAdapter(var values: List<Course>, val fm : FragmentManager) : RecyclerView.Adapter<RecyclerAdapter.CourseListItemHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): CourseListItemHolder =
        CourseListItemHolder(LayoutInflater.from(p0.context).inflate(R.layout.course_list_item, p0, false), fm)

    override fun onBindViewHolder(holder: CourseListItemHolder, position: Int) =
        holder.bind(values.get(position))

    override fun getItemCount(): Int = values.size

    fun update(values: List<Course>) {
        this.values = values
        notifyDataSetChanged()
    }

    class CourseListItemHolder(val view: View, val fm : FragmentManager) : RecyclerView.ViewHolder(view) {

        lateinit var course : Course

        lateinit var courseTitleTextView : TextView

        lateinit var detailsButton: Button
        lateinit var forumButton : Button
        lateinit var participantsButton : Button
        lateinit var filesButton : Button
        lateinit var announcementsButton : Button

        fun bind(item: Course) {

            course = item

            courseTitleTextView = view.findViewById(R.id.courseTitle)

            detailsButton = view.findViewById(R.id.detailsButton)
            forumButton = view.findViewById(R.id.forumButton)
            participantsButton = view.findViewById(R.id.participantsButton)
            filesButton = view.findViewById(R.id.filesButton)
            announcementsButton = view.findViewById(R.id.announcementsButton)

            courseTitleTextView.text = item.title

            detailsButton.setOnClickListener(View.OnClickListener {
                Timber.i("${item.title} - ${it.id} clicked")


            })

            forumButton.setOnClickListener(View.OnClickListener {
                Timber.i("${item.title} - ${it.id} clicked")
            })

            participantsButton.setOnClickListener(View.OnClickListener {
                Timber.i("${item.title} - ${it.id} clicked")
            })

            filesButton.setOnClickListener(View.OnClickListener {
                Timber.i("${item.title} - ${it.id} clicked")
            })

            announcementsButton.setOnClickListener(View.OnClickListener {
                Timber.i("${item.title} - ${it.id} clicked")
            })

            view.setOnClickListener(View.OnClickListener {
                Timber.i("${item.title} clicked")

                val fragment = CourseFragment.newInstance(Bundle(), course)

                val ft = fm.beginTransaction()
                ft.replace(R.id.content_frame, fragment)
                ft.commit()
            })
        }

    }

}