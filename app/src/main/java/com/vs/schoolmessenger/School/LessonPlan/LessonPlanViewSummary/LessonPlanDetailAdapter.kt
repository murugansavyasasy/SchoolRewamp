package com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummaryModel.LessonPlanViewSummaryDetail

class LessonPlanDetailAdapter(
    private val details: List<LessonPlanViewSummaryDetail>
) : RecyclerView.Adapter<LessonPlanDetailAdapter.DetailViewHolder>() {

    inner class DetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvKey: TextView = itemView.findViewById(R.id.namelabel)
        private val tvValue: TextView = itemView.findViewById(R.id.lblName)

        fun bind(detail: LessonPlanViewSummaryDetail) {
            tvKey.text = detail.name
            tvValue.text = detail.value
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lesson_plan_detail_item, parent, false)
        return DetailViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        holder.bind(details[position])
    }

    override fun getItemCount(): Int = details.size
}
