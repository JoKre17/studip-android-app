package de.kriegel.studip.service

import android.content.Context
import de.kriegel.studip.R
import de.kriegel.studip.client.content.model.data.Course
import de.kriegel.studip.client.content.model.data.CourseNews
import de.kriegel.studip.client.service.StudIPClient
import de.kriegel.studip.client.content.model.data.Id
import timber.log.Timber
import java.io.*


class CourseNewsManager(val studIPClient: StudIPClient, logsDirectory: File, val context: Context) {

    val historyCourseNews = ArrayList<Id>()

    val courseNewsLogsFile: File

    init {

        val courseNewsLogsFileName =
            context.resources.getString(R.string.notification_studip_course_news_logs_file_name)
        courseNewsLogsFile = File(logsDirectory.absolutePath + File.separator + courseNewsLogsFileName)

        //loadHistory(logsDirectory)

    }

    public fun getNewCourseNews(): Map<Course, List<CourseNews>> {

        val newCourseNews = HashMap<Course, List<CourseNews>>()

        studIPClient.courseService.allCourses.forEach { course ->
            run {
                if (studIPClient.courseService.getAmountCourseNewsForCourseId(course.id) > 0) {
                    val mutableCourseNewsForCourse = ArrayList<CourseNews>().toMutableList()

                    studIPClient.courseService.getAllCourseNewsForCourseId(course.id).forEach { courseNews ->
                        if (!historyCourseNews.contains(courseNews.id)) {
                            mutableCourseNewsForCourse.add(courseNews)
                            historyCourseNews.add(courseNews.id)
                        }
                    }

                    newCourseNews.put(course, mutableCourseNewsForCourse.toList())
                }
            }
        }

        //storeHistory()

        return newCourseNews
    }

    private fun loadHistory(logsDirectory: File) {

        historyCourseNews.clear()

        if (!courseNewsLogsFile.exists()) {
            val success = courseNewsLogsFile.createNewFile()

            if (!success) {
                Timber.e("Could not create file ${courseNewsLogsFile.absolutePath}")
                return
            }
        }

        courseNewsLogsFile.forEachLine {
            if (it.isEmpty()) {
                return@forEachLine
            }

            val id = Id(it.trim())
            historyCourseNews.add(id)
        }

    }

    private fun storeHistory() {

        val writer = PrintWriter(courseNewsLogsFile, "UTF-8")

        for (id in historyCourseNews) {
            writer.println(id.toString())
        }

        writer.close()

    }

}