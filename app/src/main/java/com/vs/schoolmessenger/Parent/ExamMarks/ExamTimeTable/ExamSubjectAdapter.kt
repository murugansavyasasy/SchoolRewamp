package com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTable

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkListener
import com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewampModel.ExamTimetableSubject
import com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableViewDetails.Adapter.ExamTimeTableActivityWise
import com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableViewDetails.ExamTimeTableViewDetails
import com.vs.schoolmessenger.R

class ExamSubjectAdapter(
    private var subjectList: List<ExamTimetableSubject>,
    private var context: Context,
    private val listener: ExamMarkListener,


    ) :
    RecyclerView.Adapter<ExamSubjectAdapter.SubjectViewHolder>() {

    fun updateData(newList: List<ExamTimetableSubject>) {
        subjectList = newList
        notifyDataSetChanged()
    }

    inner class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val subjectname: TextView = itemView.findViewById(R.id.subjectname)
        private val totalMarks: TextView = itemView.findViewById(R.id.totalMarks)
        private val subjectRecyclerView: RecyclerView = itemView.findViewById(R.id.subjectRecyclerView)


        fun bind(subject: ExamTimetableSubject, context: Context) {

            subjectname.text = subject.subjectName

            totalMarks.text =
                "${context.getString(R.string.total_marks)} : ${subject.total_mark}"


            val data=subject.activities?:emptyList()


            if (data.isNotEmpty()) {
                subjectRecyclerView.visibility = View.VISIBLE
                subjectRecyclerView.layoutManager =
                    LinearLayoutManager(itemView.context, LinearLayoutManager.VERTICAL, false)
                val examSubjectAdapter = ExamTimeTableActivityWise(
                    data,
                    context,listener,
                    false
                )
                subjectRecyclerView.isNestedScrollingEnabled = false
                subjectRecyclerView.adapter = examSubjectAdapter
            } else {
                subjectRecyclerView.visibility = View.GONE
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_exam_subject, parent, false)
        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        holder.bind(subjectList[position], context)
    }

    override fun getItemCount(): Int = subjectList.size
}

