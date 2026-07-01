package com.vs.schoolmessenger.Parent.ClassTestMark.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.ClassTestMark.DataClass.MarkSubjectData
import com.vs.schoolmessenger.R

class MarksReportAdapter(
    private val subjects: List<MarkSubjectData>
) : RecyclerView.Adapter<MarksReportAdapter.MarkSubjectViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarkSubjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mark_subject, parent, false)
        return MarkSubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: MarkSubjectViewHolder, position: Int) {
        holder.bind(subjects[position])
    }

    override fun getItemCount(): Int = subjects.size

    inner class MarkSubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSubjectName: TextView = itemView.findViewById(R.id.tvSubjectName)
        private val tvSubjectPercentage: TextView = itemView.findViewById(R.id.tvSubjectPercentage)
        private val activitiesContainer: LinearLayout = itemView.findViewById(R.id.activitiesContainer)
        private val tvSubjectTotal: TextView = itemView.findViewById(R.id.tvSubjectTotal)

        fun bind(subject: MarkSubjectData) {
            tvSubjectName.text = subject.subjectName

            var subjectTotalScored = 0
            var subjectTotalMax = 0
            subject.activities.forEach { activity ->
                subjectTotalScored += activity.mark.toDouble().toInt()
                subjectTotalMax += activity.maxMark.toDouble().toInt()
            }

            val percentage = if (subjectTotalMax > 0) {
                ((subjectTotalScored.toDouble() / subjectTotalMax) * 100).toInt()
            } else 0
            tvSubjectPercentage.text = "$percentage%"

            activitiesContainer.removeAllViews()
            subject.activities.forEachIndexed { index, activity ->
                val activityView = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.item_mark_activity, activitiesContainer, false)

                activityView.findViewById<TextView>(R.id.tvSerialNo).apply {
                    text = "${index + 1}"
                    textSize = 12f
                }

                activityView.findViewById<TextView>(R.id.tvActivityName).apply {
                    text = activity.activityName
                    textSize = 13f
                }

                activityView.findViewById<TextView>(R.id.tvSession).apply {
                    text = if (activity.session == "FN") "Forenoon" else "Afternoon"
                    textSize = 11f
                }

                activityView.findViewById<TextView>(R.id.tvDate).apply {
                    text = activity.examDate
                    textSize = 11f
                }

                activityView.findViewById<TextView>(R.id.tvSyllabus).apply {
                    text = activity.syllabus
                    textSize = 11f
                }

                activityView.findViewById<TextView>(R.id.tvMark).apply {
                    text = activity.mark
                    textSize = 14f
                }

                activityView.findViewById<TextView>(R.id.tvMaxMark).apply {
                    text = "${activity.maxMark.toDouble().toInt()}"
                    textSize = 12f
                }

                activitiesContainer.addView(activityView)
            }

            tvSubjectTotal.text = "$subjectTotalScored / $subjectTotalMax"
        }
    }
}