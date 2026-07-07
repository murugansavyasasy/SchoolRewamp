package com.vs.schoolmessenger.Parent.ClassTestMark.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
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
                val isAbsent = activity.attendance.equals("AB", ignoreCase = true)
                subjectTotalScored += if (isAbsent) 0 else (activity.mark.toDoubleOrNull()?.toInt() ?: 0)
                subjectTotalMax += activity.maxMark.toDoubleOrNull()?.toInt() ?: 0
            }

            val percentage = if (subjectTotalMax > 0) {
                ((subjectTotalScored.toDouble() / subjectTotalMax) * 100).toInt()
            } else 0
            tvSubjectPercentage.text = "$percentage%"

            activitiesContainer.removeAllViews()
            subject.activities.forEachIndexed { index, activity ->
                val activityView = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.item_mark_activity, activitiesContainer, false)

                val ctx = itemView.context
                val green = ContextCompat.getColor(ctx, R.color.green_pass)
                val red = ContextCompat.getColor(ctx, R.color.red_fail)
                val darkText = ContextCompat.getColor(ctx, android.R.color.black)

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

                activityView.findViewById<TextView>(R.id.tvminmarkdetails).apply {
                    text = "Min mark - " +activity.minMark
                    textSize = 11f
                }

                val tvMark = activityView.findViewById<TextView>(R.id.tvMark)
                val tvMaxMark = activityView.findViewById<TextView>(R.id.tvMaxMark)
                val viewUnderline = activityView.findViewById<View>(R.id.viewMarkUnderline)
                val imgStatusIcon = activityView.findViewById<ImageView>(R.id.imgStatusIcon)

                tvMaxMark.text = "${activity.maxMark.toDoubleOrNull()?.toInt() ?: 0}"

                val isAbsent = activity.attendance.equals("AB", ignoreCase = true)
                val markValue = activity.mark.toDoubleOrNull() ?: 0.0
                val minMarkValue = activity.minMark.toDoubleOrNull() ?: 0.0
                val isFail = !isAbsent && markValue < minMarkValue

                when {
                    isAbsent -> {
                        tvMark.text = "AB"
                        tvMark.setTextColor(red)
                        viewUnderline.setBackgroundColor(red)
                        imgStatusIcon.setImageResource(R.drawable.ic_cross_circle)
                    }
                    isFail -> {
                        tvMark.text = activity.mark
                        tvMark.setTextColor(red)
                        viewUnderline.setBackgroundColor(red)
                        imgStatusIcon.setImageResource(R.drawable.ic_cross_circle)
                    }
                    else -> {
                        tvMark.text = activity.mark
                        tvMark.setTextColor(darkText)
                        viewUnderline.setBackgroundColor(green)
                        imgStatusIcon.setImageResource(R.drawable.ic_check_circle)
                    }
                }

                activitiesContainer.addView(activityView)
            }

            tvSubjectTotal.text = "$subjectTotalScored / $subjectTotalMax"
        }
    }
}