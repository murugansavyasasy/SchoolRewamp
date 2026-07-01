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

            // Calculate subject total and percentage
            var subjectTotalScored = 0
            var subjectTotalMax = 0
            subject.activities.forEach { activity ->
                subjectTotalScored += activity.mark.toIntOrNull() ?: 0
                subjectTotalMax += activity.maxMark.toIntOrNull() ?: 0
            }

            val percentage = if (subjectTotalMax > 0) {
                ((subjectTotalScored.toDouble() / subjectTotalMax) * 100).toInt()
            } else 0
            tvSubjectPercentage.text = "$percentage%"

            // Add activities
            activitiesContainer.removeAllViews()
            subject.activities.forEachIndexed { index, activity ->
                val activityView = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.item_mark_activity, activitiesContainer, false)

                activityView.findViewById<TextView>(R.id.tvSerialNo).text = "${index + 1}"
                activityView.findViewById<TextView>(R.id.tvActivityName).text = activity.activityName
                activityView.findViewById<TextView>(R.id.tvSession).text =
                    if (activity.session == "FN") "Forenoon" else "Afternoon"
                activityView.findViewById<TextView>(R.id.tvDate).text = activity.examDate
                activityView.findViewById<TextView>(R.id.tvSyllabus).text = activity.syllabus
                activityView.findViewById<TextView>(R.id.tvMark).text = activity.mark
                activityView.findViewById<TextView>(R.id.tvMaxMark).text = "/${activity.maxMark.toInt()}"

                activitiesContainer.addView(activityView)
            }

            tvSubjectTotal.text = "$subjectTotalScored / $subjectTotalMax"
        }
    }
}