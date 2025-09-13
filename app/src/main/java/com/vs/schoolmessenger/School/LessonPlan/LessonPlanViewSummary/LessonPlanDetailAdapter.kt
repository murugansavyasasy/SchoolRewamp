package com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummaryModel.LessonPlanViewSummaryDetail
import com.vs.schoolmessenger.Utils.Constant

class LessonPlanDetailAdapter(
    private val details: List<LessonPlanViewSummaryDetail>
) : RecyclerView.Adapter<LessonPlanDetailAdapter.DetailViewHolder>() {

    inner class DetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val flowLayout: FlowLayout = itemView.findViewById(R.id.flowLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lesson_plan_detail_item, parent, false)
        return DetailViewHolder(view)
    }


    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        holder.flowLayout.removeAllViews()

        // Only include allowed items (skip Activity and Topic)
        val filteredDetails = details.filter { it.name != Constant.Activity && it.name != Constant.Topic }

        filteredDetails.forEach { detail ->
            val chipView = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.item_chip, holder.flowLayout, false)

            val imgIcon = chipView.findViewById<ImageView>(R.id.imgIcon)
            val tvText = chipView.findViewById<TextView>(R.id.tvText)

            imgIcon.setImageResource(getIconForName(detail.name))

            tvText.text = if (detail.value.isNotEmpty()) {
                "${detail.name}: ${detail.value}"
            } else {
                detail.name
            }

            holder.flowLayout.addView(chipView)
        }
    }

    private fun getIconForName(name: String): Int {
        return when (name) {
            Constant.Month -> R.drawable.ic_calendar
            Constant.Admin_Remarks -> R.drawable.id_card__1_
            Constant.From_Date -> R.drawable.time_icon
            Constant.To_Date -> R.drawable.time_icon
            Constant.Assesment -> R.drawable.tag_icon
            else -> R.drawable.ic_circle_check_mark
        }
    }

    override fun getItemCount(): Int = 1 // single card with all chips

}


