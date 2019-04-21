package de.kriegel.studip.main.course

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
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
import android.view.*
import android.view.View.*
import de.kriegel.studip.client.content.model.data.Id
import java.io.Serializable
import java.util.concurrent.atomic.AtomicBoolean


class CourseFilesFragment() : Fragment() {

    companion object {
        fun newInstance(args: Bundle, course: Course) = CourseFilesFragment().apply {
            arguments = args
            this.course = course
        }
    }

    private lateinit var course: Course
    private var courseSynchedFileRefTreeMap: Map<Id, FileRefTree>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            loadFromBundle(savedInstanceState)
        }

        if(courseSynchedFileRefTreeMap == null) {
            courseSynchedFileRefTreeMap = HashMap<Id, FileRefTree>()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.course_files_fragment, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fileRefTree = MainActivity.appConfiguration.client.courseService.getFileRefTree(course)
        val courseSynchedFileRefTreeMapMutable = courseSynchedFileRefTreeMap?.toMutableMap()
        courseSynchedFileRefTreeMapMutable?.putIfAbsent(course.id, FileRefTree(fileRefTree.root.folder))
        courseSynchedFileRefTreeMap = courseSynchedFileRefTreeMapMutable?.toMap()

        val synchedFileRefTree = courseSynchedFileRefTreeMap?.get(course.id)
        val courseFilesAdapter = CourseFilesRecyclerAdapter(course, fileRefTree, synchedFileRefTree!!, context!!)

        courseFilesRecyclerView.layoutManager = LinearLayoutManager(context)
        courseFilesRecyclerView.adapter = courseFilesAdapter
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        Timber.i("onSaveInstanceState")

        course.run {
            outState.putSerializable("course", course)
        }

        courseSynchedFileRefTreeMap.run {
            outState.putSerializable("fileRefTreeMap", courseSynchedFileRefTreeMap as Serializable)
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

        (bundle.getSerializable("fileRefTreeMap") as Map<Id, FileRefTree>)?.run {
            courseSynchedFileRefTreeMap = this
        }
    }
}

class CourseFilesRecyclerAdapter(val course: Course, val fileRefTree: FileRefTree, val synchedFileRefTree: FileRefTree, val context : Context) :
    RecyclerView.Adapter<CourseFilesRecyclerAdapter.CourseFilesViewHolder>() {

    lateinit var currentDisplayedFileRefNode : FileRefNode
    var fileRefNodeBreadCrumbs = listOf<FileRefNode>()

    init {
        currentDisplayedFileRefNode = fileRefTree.root
    }

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

    override fun getItemCount(): Int {
        // in case we are looking at the root, there is no "up" navigation element
        if(fileRefTree.root == currentDisplayedFileRefNode) {
            return currentDisplayedFileRefNode.children.size
        } else {
            return currentDisplayedFileRefNode.children.size + 1
        }
    }

    override fun onBindViewHolder(holder: CourseFilesViewHolder, position: Int) {

        // add "back"/"up" option for folder navigation
        if(position == 0 && fileRefTree.root != currentDisplayedFileRefNode) {

            holder.nameTextView.text = "..."

            holder.typeImageView.visibility = INVISIBLE
            holder.downloadButton.visibility = INVISIBLE

            holder.itemView.setOnClickListener(View.OnClickListener {
                // use last element to go up in hierarchy
                currentDisplayedFileRefNode = fileRefNodeBreadCrumbs.last()

                // remove last element
                val fileRefNodeBreadCrumbsMutable = fileRefNodeBreadCrumbs.toMutableList()
                fileRefNodeBreadCrumbsMutable.removeAt(fileRefNodeBreadCrumbs.size - 1)
                fileRefNodeBreadCrumbs = fileRefNodeBreadCrumbsMutable.toList()

                replaceItemsWithFileRefNode(currentDisplayedFileRefNode)
            })

            return
        }

        var fileRefNode : FileRefNode

        if(fileRefTree.root != currentDisplayedFileRefNode) {
            fileRefNode = currentDisplayedFileRefNode.children.get(position - 1)
        } else {
            fileRefNode = currentDisplayedFileRefNode.children.get(position)
        }


        holder.bind(fileRefNode)
        if(isFileRefNodeSynched(fileRefNode)) {
            holder.isDownloaded = true
            val updatedIdentifierColorId = R.color.baseColor
            val updatedIdentifierColor = ContextCompat.getColor(context, updatedIdentifierColorId)

            DrawableCompat.setTint(holder.downloadButton.background, updatedIdentifierColor)
        }

        if(fileRefNode.isDirectory) {
            holder.itemView.setOnClickListener(View.OnClickListener {
                val fileRefNodeBreadCrumbsMutable = fileRefNodeBreadCrumbs.toMutableList()
                fileRefNodeBreadCrumbsMutable.add(currentDisplayedFileRefNode)
                fileRefNodeBreadCrumbs = fileRefNodeBreadCrumbsMutable.toList()

                replaceItemsWithFileRefNode(fileRefNode)
            })
        }

        // on download clicked
        holder.downloadButton.setOnClickListener(OnClickListener {
            // download the files
            Timber.i("Start downloading : ${fileRefNode.fileRef.name} and children")
            val root = FileRefNode(fileRefTree.root.folder)
            root.addFileRefNode(fileRefNode)
            MainActivity.appConfiguration.client.courseService.downloadManager.downloadFileRefTree( course, FileRefTree(root), AtomicBoolean())

            // mark as being downloaded
            holder.isDownloaded = true
            val updatedIdentifierColorId = R.color.baseColor
            val updatedIdentifierColor = ContextCompat.getColor(context, updatedIdentifierColorId)

            DrawableCompat.setTint(holder.downloadButton.background, updatedIdentifierColor)

            // add to synched file ref nodes
            val parentFileRefNode = synchedFileRefTree.root
            for(fileRefNode in fileRefNodeBreadCrumbs) {
                if(!parentFileRefNode.children.contains(fileRefNode)) {
                    parentFileRefNode.children.add(fileRefNode)
                }
            }
            if(!parentFileRefNode.children.contains(fileRefNode)) {
                parentFileRefNode.children.add(fileRefNode)
            }
        })

    }

    /**
     * Checks, if a file (fileRefNode) is already being synched / downloaded
     */
    fun isFileRefNodeSynched(fileRefNode: FileRefNode) : Boolean {

        var fileRefNodes = mutableListOf<FileRefNode>(synchedFileRefTree.root)
        while (fileRefNodes.isNotEmpty()) {
            val currentNode = fileRefNodes.removeAt(0)

            if(fileRefNode.equals(currentNode)) {
                return true
            }

            if(currentNode.isDirectory) {
                fileRefNodes.addAll(currentNode.children)
            }
        }

        return false
    }

    fun replaceItemsWithFileRefNode(fileRefNode : FileRefNode) {
        currentDisplayedFileRefNode = fileRefNode

        notifyDataSetChanged()
    }

}