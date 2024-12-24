package com.vs.schoolmessenger.School.ExamSchedule

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.vs.schoolmessenger.R

class ExamSubjectListAdapter(
    private val itemList: List<ExamSubjectNameData>?,
    private val context: Context,
    private val listener: ExamSubjectListClickListener
) : BaseAdapter() {

    override fun getCount(): Int {
        return itemList?.size ?: 0
    }

    override fun getItem(position: Int): Any? {
        return itemList?.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.exam_subject_list_item, parent, false)

        val lblSubjectName: TextView = view.findViewById(R.id.lblSubjectName)
        val data = itemList?.get(position)

        lblSubjectName.text = data?.isSubjectName

        // Handle item clicks
        view.setOnClickListener {
            if (data != null) {
                listener.onItemClick(data)
            }
        }

        return view
    }
}

