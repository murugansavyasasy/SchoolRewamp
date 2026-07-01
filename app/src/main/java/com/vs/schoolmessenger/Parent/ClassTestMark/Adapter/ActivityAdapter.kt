package com.vs.schoolmessenger.Parent.ClassTestMark.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.ClassTestMark.DataClass.TestActivityData
import com.vs.schoolmessenger.R

class ActivityAdapter(
    private val activities: List<TestActivityData>
) : RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(activities[position])
    }

    override fun getItemCount(): Int = activities.size

    inner class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvActivityName: TextView = itemView.findViewById(R.id.tvActivityName)
        private val tvSession: TextView = itemView.findViewById(R.id.tvSession)
        private val sessionContainer: LinearLayout = itemView.findViewById(R.id.sessionContainer)
        private val tvExamDate: TextView = itemView.findViewById(R.id.tvExamDate)
        private val tvSyllabus: TextView = itemView.findViewById(R.id.tvSyllabus)
        private val tvMaxMark: TextView = itemView.findViewById(R.id.tvMaxMark)
        private val tvMinMark: TextView = itemView.findViewById(R.id.tvMinMark)

        fun bind(activity: TestActivityData) {
            tvActivityName.text = activity.activityName
            tvExamDate.text = activity.examDate
            tvSyllabus.text = activity.syllabus
            tvMaxMark.text = activity.maxMark.toInt().toString()
            tvMinMark.text = activity.minMark.toInt().toString()

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
        }
    }
}