package com.vs.schoolmessenger.Parent.ExamMarks

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTable.ExamSubjectAdapter
import com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewampModel.ExamTimetable
import com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewampModel.ExamTimetableSubject
import com.vs.schoolmessenger.R

class ExamTimeTableAdapter(
    private var fullList: List<ExamTimetable>,
    private var context: Context,
    private val listener: ExamMarkListener
) :
    RecyclerView.Adapter<ExamTimeTableAdapter.ExamTimeTableViewHolder>(), Filterable {

    private var filteredList: List<ExamTimetable> = fullList
    private var subject: List<ExamTimetableSubject> = emptyList()

    inner class ExamTimeTableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val subjectName: TextView = itemView.findViewById(R.id.subjectName)
        private val subjectRecyclerView: RecyclerView =
            itemView.findViewById(R.id.subjectRecyclerView)

        fun bind(examTimeTable: ExamTimetable, context: Context) {
            subjectName.text = examTimeTable.examName
            subject=examTimeTable.subjects?:emptyList()

            if (subject.isNotEmpty()) {
                subjectRecyclerView.visibility = View.VISIBLE
                subjectRecyclerView.layoutManager =
                    LinearLayoutManager(itemView.context, LinearLayoutManager.VERTICAL, false)
                val examSubjectAdapter = ExamSubjectAdapter(
                    subject,
                    this@ExamTimeTableAdapter.context
                )
                subjectRecyclerView.isNestedScrollingEnabled = false
                subjectRecyclerView.adapter = examSubjectAdapter
            } else {
                subjectRecyclerView.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamTimeTableViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.exam_time_table_item, parent, false)
        return ExamTimeTableViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExamTimeTableViewHolder, position: Int) {
        holder.bind(filteredList[position], context)
    }

    override fun getItemCount(): Int = filteredList.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""

                val result = if (query.isEmpty()) {
                    fullList
                } else {
                    fullList.filter { exam ->
                        exam.examName.orEmpty().lowercase().contains(query) ||
                                exam.subjects.orEmpty().any { subject ->
                                    subject.subjectName.orEmpty().lowercase().contains(query) ||
                                            subject.max_mark?.toString().orEmpty().lowercase().contains(query)
                                }
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = result
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<ExamTimetable> ?: listOf()
                notifyDataSetChanged()
                listener.onSearchResultEmpty(filteredList.isEmpty())
            }
        }
    }
}

//Old Source
//package com.vs.schoolmessenger.Parent.ExamMarks
//
//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Filter
//import android.widget.Filterable
//import android.widget.TextView
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTable.ExamSubjectAdapter
//import com.vs.schoolmessenger.Parent.ExamMarks.Model.ExamData
//import com.vs.schoolmessenger.R
//
//class ExamTimeTableAdapter(
//    private var fullList: List<ExamData>,
//    private var context: Context,
//    private val listener: ExamMarkListener
//) :
//    RecyclerView.Adapter<ExamTimeTableAdapter.ExamTimeTableViewHolder>(), Filterable {
//
//    private var filteredList: List<ExamData> = fullList
//
//    inner class ExamTimeTableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val subjectName: TextView = itemView.findViewById(R.id.subjectName)
//        private val subjectRecyclerView: RecyclerView =
//            itemView.findViewById(R.id.subjectRecyclerView)
//
//        fun bind(examTimeTable: ExamData, context: Context) {
//            subjectName.text = examTimeTable.name
//
//            if (examTimeTable.exam_subject_details.isNotEmpty()) {
//                subjectRecyclerView.visibility = View.VISIBLE
//                subjectRecyclerView.layoutManager =
//                    LinearLayoutManager(itemView.context, LinearLayoutManager.VERTICAL, false)
//                val examSubjectAdapter = ExamSubjectAdapter(
//                    examTimeTable.exam_subject_details,
//                    this@ExamTimeTableAdapter.context
//                )
//                subjectRecyclerView.isNestedScrollingEnabled = false
//                subjectRecyclerView.adapter = examSubjectAdapter
//            } else {
//                subjectRecyclerView.visibility = View.GONE
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamTimeTableViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.exam_time_table_item, parent, false)
//        return ExamTimeTableViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ExamTimeTableViewHolder, position: Int) {
//        holder.bind(filteredList[position], context)
//    }
//
//    override fun getItemCount(): Int = filteredList.size
//
//    override fun getFilter(): Filter {
//        return object : Filter() {
//            override fun performFiltering(constraint: CharSequence?): FilterResults {
//                val query = constraint?.toString()?.lowercase()?.trim() ?: ""
//
//                val result = if (query.isEmpty()) {
//                    fullList
//                } else {
//                    fullList.filter { exam ->
//                        exam.name.lowercase().contains(query) ||
//                                exam.exam_subject_details.any { subject ->
//                                    subject.subject_name.lowercase().contains(query) ||
//                                            subject.exam_date.lowercase().contains(query) ||
//                                            subject.max_mark.lowercase().contains(query) ||
//                                            subject.syllabus.lowercase().contains(query)
//                                }
//                    }
//                }
//
//                val filterResults = FilterResults()
//                filterResults.values = result
//                return filterResults
//            }
//
//            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
//                filteredList = results?.values as? List<ExamData> ?: listOf()
//                notifyDataSetChanged()
//                listener.onSearchResultEmpty(filteredList.isEmpty())
//            }
//        }
//    }
//}
