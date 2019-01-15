package de.kriegel.studip.main.course

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.kriegel.studip.R
import de.kriegel.studip.client.content.model.data.Course
import de.kriegel.studip.client.content.model.data.Id
import de.kriegel.studip.main.MainActivity
import kotlinx.android.synthetic.main.course_fragment.*
import timber.log.Timber
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList
import android.R.attr.rating



class CourseFragment() : Fragment() {

    companion object {
        fun newInstance(args: Bundle, courses: List<Course>) = CourseFragment().apply {
            Timber.i("newInstance")
            arguments = args

            this.courses = courses

            Timber.i(courses.toString())
        }
    }

    private var courses: List<Course>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate")

        if (savedInstanceState != null) {
            loadFromBundle(savedInstanceState)
        } else {
            if(courses == null) {
                courses = ArrayList()
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.course_fragment, container, false)
        Timber.d("onCreateView")

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated")

        val viewManager = LinearLayoutManager(context)
        val viewAdapter = RecyclerAdapter(courses!!)

        recyclerView.apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Timber.d("onSaveInstanceState")

        courses?.run {
            var courseIds = ArrayList<Id>().toMutableList()
            forEach { courseIds.add(it.id) }

            outState.putSerializable("courseIds", courseIds as Serializable)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if(savedInstanceState != null) {
            loadFromBundle(savedInstanceState)
        }
    }

    private fun loadFromBundle(bundle: Bundle) {
        Timber.d("loadFromBundle")

        (bundle.getSerializable("courseIds") as List<Id>)?.run {
            val coursesMutable = ArrayList<Course>().toMutableList()
            forEach {
                Timber.i("Loading course $it")
                coursesMutable.add(MainActivity.client.courseService.getCourseById(it))
            }
            courses = coursesMutable.toList()
        }

    }

}

class RecyclerAdapter(val values: List<Course>) : RecyclerView.Adapter<RecyclerAdapter.StringHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): StringHolder =
        StringHolder(LayoutInflater.from(p0.context).inflate(android.R.layout.simple_list_item_1, p0, false))

    override fun onBindViewHolder(holder: StringHolder, position: Int) =
        holder.bind(values.get(position).title)

    override fun getItemCount(): Int = values.size

    class StringHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: String) = with(view as TextView) {
            view.text = item
        }

    }

}