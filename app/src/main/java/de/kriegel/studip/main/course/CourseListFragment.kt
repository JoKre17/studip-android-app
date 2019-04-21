package de.kriegel.studip.main.course

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.*
import de.kriegel.studip.R
import de.kriegel.studip.client.content.model.data.Course
import de.kriegel.studip.main.MainActivity
import kotlinx.android.synthetic.main.course_list_fragment.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.Serializable
import kotlin.collections.ArrayList
import de.kriegel.studip.client.content.model.data.Semester

class CourseListFragment() : Fragment(), AdapterView.OnItemSelectedListener {

    companion object {
        fun newInstance(args: Bundle) = CourseListFragment().apply {
            arguments = args
        }
    }

    private var courses: List<Course>? = null
    private var semesters : List<Semester>? = null
    private var selectedSemester: Semester? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        if (savedInstanceState != null) {
            loadFromBundle(savedInstanceState)
        } else {
            /*
            if (courses == null) {
                val client = MainActivity.appConfiguration.client

                (activity as CoroutineScope).launch(Dispatchers.Default) {
                    val currentSemester = client.courseService.currentSemester
                    val currentCourses = client.courseService.getCoursesForSemesterId(currentSemester.id)
                    courses = currentCourses

                    Timber.d("Got ${currentCourses.size} courses")

                    (activity as CoroutineScope).launch(Dispatchers.Main) {
                        (recyclerView.adapter as CourseListRecyclerAdapter).update(currentCourses)
                        loadingPanel.visibility = View.GONE
                    }
                }
            }
            */
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.course_list_fragment_menu_items, menu)

        if(semesters != null) {
            return
        }

        val item = menu.findItem(R.id.spinner)
        val spinner = item.actionView as Spinner

        val client = MainActivity.appConfiguration.client

        val onItemSelectedListener = this

        (activity as CoroutineScope).launch(Dispatchers.Default) {
            var semestersMutable = client.courseService.allSemesters
            semestersMutable.sortByDescending { it.seminars_begin }
            semesters = semestersMutable.toList()

            Timber.d("Got ${semestersMutable.size} semesters")

            val adapter =
                ArrayAdapter<Semester>(activity, R.layout.layout_drop_title, semestersMutable)
            adapter.setDropDownViewResource(R.layout.layout_drop_list)

            (activity as CoroutineScope).launch(Dispatchers.Main) {
                spinner.adapter = adapter
                if(selectedSemester != null) {
                    spinner.setSelection(semestersMutable.indexOf(selectedSemester))
                }
            }

            spinner.onItemSelectedListener = onItemSelectedListener
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

        // no need to reload if the current selected semester is the same as the currently displayed one
        if (selectedSemester != null && selectedSemester == (p0?.selectedItem as Semester)) {
            return
        }

        selectedSemester = p0?.selectedItem as Semester

        val client = MainActivity.appConfiguration.client

        (activity as CoroutineScope).launch(Dispatchers.Default) {

            (activity as CoroutineScope).launch(Dispatchers.Main) {
                (recyclerView.adapter as CourseListRecyclerAdapter).update(listOf())
                loadingPanel.visibility = View.VISIBLE
            }

            val currentCourses = client.courseService.getCoursesForSemesterId(selectedSemester?.id)
            courses = currentCourses

            Timber.d("Got ${currentCourses.size} courses")

            (activity as CoroutineScope).launch(Dispatchers.Main) {
                (recyclerView.adapter as CourseListRecyclerAdapter).update(currentCourses)
                loadingPanel.visibility = View.GONE
            }
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.course_list_fragment, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var coursesImmutable: List<Course> = ArrayList()
        if (courses != null) {
            coursesImmutable = courses!!
            loadingPanel.visibility = View.GONE
        }

        val viewManager = LinearLayoutManager(context)
        val viewAdapter = CourseListRecyclerAdapter(coursesImmutable, fragmentManager!!, context!!)

        recyclerView.apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        courses?.run {
            outState.putSerializable("courses", courses as Serializable)
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

        (bundle.getSerializable("courses") as List<Course>).run {
            val coursesMutable = ArrayList<Course>().toMutableList()
            forEach {
                Timber.i("Loading course $it")
                coursesMutable.add(it)
            }
            courses = coursesMutable.toList()
        }

    }

}

class CourseListRecyclerAdapter(var values: List<Course>, val fm: FragmentManager, val context : Context) :
    RecyclerView.Adapter<CourseListRecyclerAdapter.CourseListItemHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): CourseListItemHolder =
        CourseListItemHolder(LayoutInflater.from(p0.context).inflate(R.layout.course_list_item, p0, false), fm)

    override fun onBindViewHolder(holder: CourseListItemHolder, position: Int) =
        holder.bind(values.get(position), context)

    override fun getItemCount(): Int = values.size

    fun update(values: List<Course>) {
        this.values = values
        notifyDataSetChanged()
    }

    class CourseListItemHolder(val view: View, val fm: FragmentManager) : RecyclerView.ViewHolder(view) {

        lateinit var course: Course

        lateinit var courseTitleTextView: TextView

        lateinit var detailsButton: Button
        lateinit var forumButton: Button
        lateinit var participantsButton: Button
        lateinit var filesButton: Button
        lateinit var announcementsButton: Button

        fun bind(item: Course, context: Context) {

            course = item

            courseTitleTextView = view.findViewById(R.id.courseTitle)

            detailsButton = view.findViewById(R.id.detailsButton)
            forumButton = view.findViewById(R.id.forumButton)
            participantsButton = view.findViewById(R.id.participantsButton)
            filesButton = view.findViewById(R.id.filesButton)
            announcementsButton = view.findViewById(R.id.announcementsButton)


            val updatedIdentifierColorId = R.color.baseColor
            val updatedIdentifierColor = ContextCompat.getColor(context, updatedIdentifierColorId)

            DrawableCompat.setTint(detailsButton.background, updatedIdentifierColor)
            DrawableCompat.setTint(participantsButton.background, updatedIdentifierColor)

            (context as CoroutineScope).launch(Dispatchers.Default) {

                val client = MainActivity.appConfiguration.client

                var newFiles = false
                var newNotifications = false

                if(client.courseService.getFileRefTree(course).size > 1) {
                    newFiles = true
                }

                // check for new notifications


                (context as CoroutineScope).launch(Dispatchers.Main) {
                    if(newFiles) {
                        DrawableCompat.setTint(filesButton.background, updatedIdentifierColor)
                    }

                    if(newNotifications) {
                        DrawableCompat.setTint(announcementsButton.background, updatedIdentifierColor)
                    }
                }
            }


            courseTitleTextView.text = item.title

            detailsButton.setOnClickListener(View.OnClickListener {
                Timber.d("${item.title} - details clicked")
                showCourseFragment(0)
            })

            forumButton.setOnClickListener(View.OnClickListener {
                Timber.d("${item.title} - forum clicked")
                showCourseFragment(1)
            })

            participantsButton.setOnClickListener(View.OnClickListener {
                Timber.d("${item.title} - participants clicked")
                showCourseFragment(2)
            })

            filesButton.setOnClickListener(View.OnClickListener {
                Timber.d("${item.title} - files clicked")
                showCourseFragment(3)
            })

            announcementsButton.setOnClickListener(View.OnClickListener {
                Timber.d("${item.title} - announcements clicked")
                showCourseFragment(4)
            })

            // on default click on the list item, just open the details view
            view.setOnClickListener(View.OnClickListener {
                Timber.d("${item.title} clicked")
                showCourseFragment(0)
            })
        }

        fun showCourseFragment(i: Int) {
            val fragment = CourseFragment.newInstance(Bundle(), course, initialShownTabIndex = i)

            val ft = fm.beginTransaction()
            ft.replace(R.id.content_frame, fragment)
            ft.commit()
        }

    }

}