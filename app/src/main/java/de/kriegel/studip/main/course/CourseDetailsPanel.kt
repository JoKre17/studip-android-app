package de.kriegel.studip.main.course

import android.content.Context
import android.graphics.Typeface
import android.support.v7.widget.CardView
import android.text.Spanned
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import de.kriegel.studip.R
import kotlinx.android.synthetic.main.course_details_panel.view.*
import timber.log.Timber






class CourseDetailsPanel(context : Context, val title : String, val contentList : List<Pair<String, Any>>) : CardView(context) {

    private val root : View

    init {
        root = View.inflate(context, R.layout.course_details_panel, this)

        layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        (layoutParams as ViewGroup.MarginLayoutParams).setMargins(0, 0, 0, 32)

        titleTextView.text = title

        contentList.forEach {
            val tableRow = TableRow(context)
            tableRow.setLayoutParams(
                TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
            )

            val keyTextView = TextView(context)
            keyTextView.setTypeface(null, Typeface.BOLD)
            keyTextView.setLayoutParams(TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f))
            tableRow.addView(keyTextView)

            if (it.first.equals("empty")) {
                setTextViewText(keyTextView, it.second)
            } else {
                keyTextView.text = it.first

                val valueTextView = TextView(context)
                setTextViewText(valueTextView, it.second)
                valueTextView.setLayoutParams(TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f))
                tableRow.addView(valueTextView)
            }

            tableLayout.addView(tableRow,
                TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT))
        }

    }

    private fun setTextViewText(textView : TextView, content : Any) {
        if(content is Spanned) {
            textView.text = content as Spanned
        } else {
            textView.text = content.toString()
        }
    }

}