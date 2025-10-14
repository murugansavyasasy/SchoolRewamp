package com.vs.schoolmessenger.School.AbsenteesReport.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
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

    private var selectedPosition: Int = if (items.isNotEmpty()) 0 else RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbsenteeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.absentees_detail_list, parent, false)
        return AbsenteeViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    inner class AbsenteeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvClassName = itemView.findViewById<TextView>(R.id.classvalue)
        private val tvSectionName = itemView.findViewById<TextView>(R.id.sectionvalue)
        private val tvAbsentCount = itemView.findViewById<TextView>(R.id.absentvalue)
        private val progressBar = itemView.findViewById<ProgressBar>(R.id.progressAbsent)
        private val root = itemView.findViewById<LinearLayout>(R.id.root)

        fun bind(
            classWise: ClassWise,
            sectionWise: SectionWise,
            selectedDate: String,
            listener: OnAbsenteeClickListener,
            isSelected: Boolean
        ) {
            tvClassName.text = "Class : ${classWise.class_name}"
            tvSectionName.text = "Section : ${sectionWise.section_name}"
            val absent = sectionWise.total_absentees.toIntOrNull() ?: 0
            val total = sectionWise.student_counts.toIntOrNull() ?: 1
            tvAbsentCount.text = "Absent : $absent / $total"
            progressBar.max = total
            progressBar.progress = absent

            if (isSelected) {
                root.background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_card_container)
                tvClassName.setTextColor(ContextCompat.getColor(tvClassName.context, R.color.white))
                tvSectionName.setTextColor(ContextCompat.getColor(tvSectionName.context, R.color.white))
                tvAbsentCount.setTextColor(ContextCompat.getColor(tvAbsentCount.context, R.color.PrimaryColor))
                progressBar.visibility = View.VISIBLE
            } else {
                root.background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_card_containe_unselectedr)
                tvClassName.setTextColor(ContextCompat.getColor(tvClassName.context, R.color.black))
                tvSectionName.setTextColor(ContextCompat.getColor(tvSectionName.context, R.color.black))
                tvAbsentCount.setTextColor(ContextCompat.getColor(tvAbsentCount.context, R.color.black))
                progressBar.visibility = View.INVISIBLE
            }

            itemView.setOnClickListener {
                if (bindingAdapterPosition == selectedPosition) return@setOnClickListener
                val previousPosition = selectedPosition
                selectedPosition = bindingAdapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                listener.onAbsenteeClicked(
                    selectedDate,
                    sectionWise.section_id,
                    classWise.class_name,
                    sectionWise.section_name,
                    sectionWise.student_counts,
                    absent.toString(),
                    total.toString()
                )
            }
        }
    }

    override fun onBindViewHolder(holder: AbsenteeViewHolder, position: Int) {
        val (classWise, sectionWise) = items[position]
        holder.bind(
            classWise,
            sectionWise,
            selectedDate,
            listener,
            position == selectedPosition
        )
    }
}
