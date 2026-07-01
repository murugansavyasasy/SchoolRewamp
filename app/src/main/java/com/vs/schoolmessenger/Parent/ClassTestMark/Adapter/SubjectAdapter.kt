package com.vs.schoolmessenger.Parent.ClassTestMark.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.ClassTestMark.DataClass.TestSubjectData
import com.vs.schoolmessenger.R

class SubjectAdapter(
    private val subjects: List<TestSubjectData>
) : RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder>() {

    private val expandedPositions = subjects.indices.toMutableSet()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject_header, parent, false)
        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        holder.bind(subjects[position], position)
    }

    override fun getItemCount(): Int = subjects.size

    inner class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSubjectName: TextView = itemView.findViewById(R.id.tvSubjectName)
        private val tvActivityCount: TextView = itemView.findViewById(R.id.tvActivityCount)
        private val ivSubjectExpand: ImageView = itemView.findViewById(R.id.ivSubjectExpand)
        private val activitiesContainer: LinearLayout = itemView.findViewById(R.id.activitiesContainer)

        fun bind(subject: TestSubjectData, position: Int) {
            tvSubjectName.text = subject.subjectName
            val activityCount = subject.activities.size
            tvActivityCount.text = "$activityCount ${if (activityCount == 1) "activity" else "activities"}"

            val isExpanded = expandedPositions.contains(position)

            ivSubjectExpand.rotation = if (isExpanded) 180f else 0f

            activitiesContainer.visibility = if (isExpanded) View.VISIBLE else View.GONE

            activitiesContainer.removeAllViews()
            if (isExpanded) {
                subject.activities.forEach { activity ->
                    val activityView = LayoutInflater.from(itemView.context)
                        .inflate(R.layout.item_activity, activitiesContainer, false)

                    activityView.findViewById<TextView>(R.id.tvActivityName).text = activity.activityName
                    activityView.findViewById<TextView>(R.id.tvExamDate).text = activity.examDate
                    activityView.findViewById<TextView>(R.id.tvSyllabus).text = activity.syllabus

                    activityView.findViewById<TextView>(R.id.tvMaxMark).text = activity.maxMark.toDouble().toInt().toString()
                    activityView.findViewById<TextView>(R.id.tvMinMark).text = activity.minMark.toDouble().toInt().toString()

                    val sessionContainer = activityView.findViewById<LinearLayout>(R.id.sessionContainer)
                    val tvSession = activityView.findViewById<TextView>(R.id.tvSession)

                    when (activity.session) {
                        "FN" -> {
                            tvSession.text = "Forenoon"
                            sessionContainer.setBackgroundResource(R.drawable.bg_session_forenoon)
                            tvSession.setTextColor(itemView.context.getColor(R.color.blue_session))
                        }
                        "AN" -> {
                            tvSession.text = "Afternoon"
                            sessionContainer.setBackgroundResource(R.drawable.bg_session_afternoon)
                            tvSession.setTextColor(itemView.context.getColor(R.color.orange_session))
                        }
                    }

                    activitiesContainer.addView(activityView)
                }
            }

            itemView.setOnClickListener {
                if (expandedPositions.contains(position)) {
                    expandedPositions.remove(position)
                } else {
                    expandedPositions.add(position)
                }
                notifyItemChanged(position)
            }
        }
    }
}