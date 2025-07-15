package com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTable

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.ExamMarks.Model.ExamSubjectDetail
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant

class ExamSubjectAdapter(private var subjectList: List<ExamSubjectDetail>) :
    RecyclerView.Adapter<ExamSubjectAdapter.SubjectViewHolder>() {

    fun updateData(newList: List<ExamSubjectDetail>) {
        subjectList = newList
        notifyDataSetChanged()
    }

    inner class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val subjectname: TextView = itemView.findViewById(R.id.subjectname)
        private val datevalue: TextView = itemView.findViewById(R.id.datevalue)
        private val syllabusvalue: TextView = itemView.findViewById(R.id.syllabusvalue)
        private val maxmarkvalue: TextView = itemView.findViewById(R.id.maxmarkvalue)

        fun bind(subject: ExamSubjectDetail) {
            subjectname.text = subject.subject_name
            datevalue.text =  Constant.convertDateFormat1("Date & Time : ${subject.exam_date}")
            syllabusvalue.text = "Syllabus : ${subject.syllabus}"
            maxmarkvalue.text = "Max Mark ${subject.max_mark}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_exam_subject, parent, false)
        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        holder.bind(subjectList[position])
    }

    override fun getItemCount(): Int = subjectList.size
}
