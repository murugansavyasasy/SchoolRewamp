package com.vs.schoolmessenger.Parent.Attendance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.Attendance.Model.GetWeekStatusData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant

class WeekStatusAdapter(private val items: List<GetWeekStatusData>) :
    RecyclerView.Adapter<WeekStatusAdapter.WeekStatusViewHolder>() {

    inner class WeekStatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lblDay: TextView = itemView.findViewById(R.id.lblDay)
        val lnrBackground: View = itemView.findViewById(R.id.lnrBackground)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekStatusViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.week_status_item, parent, false)
        return WeekStatusViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeekStatusViewHolder, position: Int) {
        val item = items[position]
        val status = item.status.trim()
        holder.lblDay.text = item.day

        if (status == Constant.x || status == Constant.X_) {
            holder.lnrBackground.setBackgroundResource(R.drawable.present_icon)
        } else if (status == Constant.school) {
            holder.lnrBackground.setBackgroundResource(R.drawable.absent_icon)
        } else if (status == Constant.iffin) {
            holder.lnrBackground.setBackgroundResource(R.drawable.not_taken_icon)
        } else if (status == Constant.slash) {
            holder.lnrBackground.setBackgroundResource(R.drawable.first_half_icon)
        } else if (status == Constant.secondHalf) {
            holder.lnrBackground.setBackgroundResource(R.drawable.second_half_icon)
        } else if (status == Constant.section) {
            holder.lnrBackground.setBackgroundResource(R.drawable.holiday_icon)
        }
    }

    override fun getItemCount(): Int = items.size
}
