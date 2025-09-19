package com.vs.schoolmessenger.School.AbsenteesReport.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.AbsenteesReport.Listener.OnAbsenteeClickListener
import com.vs.schoolmessenger.School.AbsenteesReport.Model.ClassWise
import com.vs.schoolmessenger.School.AbsenteesReport.Model.SectionWise



class AbsenteesReportDetailAdapter(
    private val items: List<Pair<ClassWise, SectionWise>>,
    private val selectedDate: String,
    private val listener: OnAbsenteeClickListener
) : RecyclerView.Adapter<AbsenteesReportDetailAdapter.AbsenteeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbsenteeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.absentees_detail_list, parent, false)
        return AbsenteeViewHolder(view)
    }


    override fun getItemCount(): Int = items.size

    class AbsenteeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvClassName = itemView.findViewById<TextView>(R.id.classvalue)
        private val tvSectionName = itemView.findViewById<TextView>(R.id.sectionvalue)
        private val tvAbsentCount = itemView.findViewById<TextView>(R.id.absentvalue)
        private val progressBar = itemView.findViewById<ProgressBar>(R.id.progressAbsent)

        fun bind(
            classWise: ClassWise,
            sectionWise: SectionWise,
            selectedDate: String,
            listener: OnAbsenteeClickListener
        ) {
            tvClassName.text = "Class : ${classWise.class_name}"
            tvSectionName.text = "Section : ${sectionWise.section_name}"
            val absent = sectionWise.total_absentees.toIntOrNull() ?: 0
            val total = classWise.student_counts.toIntOrNull() ?: 1
            tvAbsentCount.text = "Absent : $absent / $total"
            progressBar.max = total
            progressBar.progress = absent

            itemView.setOnClickListener {
                listener.onAbsenteeClicked(
                    selectedDate,   // ✅ send selected date
                    sectionWise.section_id
                )
            }
        }
    }

    override fun onBindViewHolder(holder: AbsenteeViewHolder, position: Int) {
        val (classWise, sectionWise) = items[position]
        holder.bind(classWise, sectionWise, selectedDate, listener)
    }

}
