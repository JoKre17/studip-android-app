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
import de.kriegel.studip.client.content.model.data.CourseMemberType
import de.kriegel.studip.client.content.model.data.User
import timber.log.Timber
import de.kriegel.studip.main.MainActivity
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection
import kotlinx.android.synthetic.main.activity_login.view.*


class CourseParticipantsFragment() : Fragment() {

    companion object {
        fun newInstance(args: Bundle, course: Course) = CourseParticipantsFragment().apply {
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
        val view = inflater.inflate(R.layout.course_participants_fragment, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sectionedRecyclerViewAdapter = SectionedRecyclerViewAdapter()

        val membersMap = MainActivity.appConfiguration.client.courseService.getAllMembersForCourseId(course.id)
        membersMap.toSortedMap(compareBy<CourseMemberType> { it.ordinal }.reversed()).forEach { courseMemberType, membersList ->

            val memberSection = CourseMemberSection(
                courseMemberType.getName().capitalize(),
                membersList.sortedBy { it.userInformation.family })

            sectionedRecyclerViewAdapter.addSection(memberSection)
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.courseParticipantsRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = sectionedRecyclerViewAdapter
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

class CourseMemberSection(val sectionTitle: String, val userList: List<User>) : StatelessSection(
    SectionParameters.builder()
        .itemResourceId(R.layout.course_participants_list_item)
        .headerResourceId(R.layout.course_participants_list_header)
        .build()
) {

    override fun getContentItemsTotal(): Int {
        return userList.size // number of items of this section
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder {
        // return a custom instance of ViewHolder for the items of this section
        return CourseParticipantsListItem(view)
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as CourseParticipantsListItem

        // bind your view here
        viewHolder.numberTextView.text = String.format("%03d", position+1)

        val userInfo = userList.get(position).userInformation
        viewHolder.nameTextView.text = "${userInfo.family}, ${userInfo.given}"
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder?) {
        super.onBindHeaderViewHolder(holder)

        holder?.itemView?.findViewById<TextView>(R.id.headerTextView)?.text = sectionTitle
    }
}

class CourseParticipantsListItem(view: View) : RecyclerView.ViewHolder(view) {

    var nameTextView: TextView
    var numberTextView: TextView

    init {
        nameTextView = view.findViewById(R.id.nameTextView)
        numberTextView = view.findViewById(R.id.numberTextView)
    }

}
