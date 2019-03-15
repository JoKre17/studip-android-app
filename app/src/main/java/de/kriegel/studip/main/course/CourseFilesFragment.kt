package de.kriegel.studip.main.course

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import de.kriegel.studip.R
import de.kriegel.studip.client.content.model.data.Course
import de.kriegel.studip.client.content.model.file.FileRefNode
import de.kriegel.studip.client.content.model.file.FileRefTree
import de.kriegel.studip.main.MainActivity
import kotlinx.android.synthetic.main.course_files_fragment.*
import timber.log.Timber

class CourseFilesFragment() : Fragment() {

    companion object {
        fun newInstance(args: Bundle, course: Course) = CourseFilesFragment().apply {
            arguments = args
            this.course = course
        }
    }

    private lateinit var course: Course

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.course_files_fragment, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fileRefTree = MainActivity.appConfiguration.client.courseService.getFileRefTree(course)
        val courseFilesAdapter = CourseFilesRecyclerAdapter(fileRefTree)

        courseFilesRecyclerView.layoutManager = LinearLayoutManager(context)
        courseFilesRecyclerView.adapter = courseFilesAdapter
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

class CourseFilesRecyclerAdapter(val fileRefTree: FileRefTree) :
    RecyclerView.Adapter<CourseFilesRecyclerAdapter.CourseFilesViewHolder>() {

    class CourseFilesViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val typeImageView : ImageView
        val nameTextView : TextView
        val downloadButton : Button

        var isDownloaded: Boolean = false
        var isDirectory: Boolean = false

        init {
            typeImageView = view.findViewById<ImageView>(R.id.typeImageView)
            nameTextView = view.findViewById<TextView>(R.id.nameTextView)
            downloadButton = view.findViewById<Button>(R.id.downloadButton)
        }

        fun bind(fileRefNode: FileRefNode) {
            this.isDownloaded = isDownloaded

            isDirectory = fileRefNode.isDirectory

            if (isDirectory) {
                typeImageView.setImageResource(R.drawable.ic_folder)
                nameTextView.text = fileRefNode.folder.nameValidAsFilename
            } else {
                typeImageView.setImageResource(R.drawable.ic_document)
                nameTextView.text = fileRefNode.fileRef.name
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseFilesRecyclerAdapter.CourseFilesViewHolder =
        CourseFilesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.course_files_list_item, parent, false))

    override fun getItemCount(): Int = fileRefTree.root.children.size

    override fun onBindViewHolder(holder: CourseFilesViewHolder, position: Int) {
        holder.bind(fileRefTree.root.children.get(position))
    }

}