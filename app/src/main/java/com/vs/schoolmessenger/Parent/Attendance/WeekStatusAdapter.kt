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

        when (status.uppercase()) {
            "P/P" -> holder.lnrBackground.setBackgroundResource(R.drawable.present_icon)
            "P/A" -> holder.lnrBackground.setBackgroundResource(R.drawable.p_and_a)
            "P/OD" -> holder.lnrBackground.setBackgroundResource(R.drawable.p_and_od)
            "P/P~" -> holder.lnrBackground.setBackgroundResource(R.drawable.p_and_l)
            "P/-" -> holder.lnrBackground.setBackgroundResource(R.drawable.p_and_n)

            "A/P" -> holder.lnrBackground.setBackgroundResource(R.drawable.a_and_p)
            "A/A" -> holder.lnrBackground.setBackgroundResource(R.drawable.absent_icon)
            "A/OD" -> holder.lnrBackground.setBackgroundResource(R.drawable.a_and_od)
            "A/P~" -> holder.lnrBackground.setBackgroundResource(R.drawable.a_and_l)
            "A/-" -> holder.lnrBackground.setBackgroundResource(R.drawable.a_and_n)

            "P~/P" -> holder.lnrBackground.setBackgroundResource(R.drawable.l_and_p)
            "P~/A" -> holder.lnrBackground.setBackgroundResource(R.drawable.l_and_a)
            "P~/OD" -> holder.lnrBackground.setBackgroundResource(R.drawable.l_and_od)
            "P~/P~" -> holder.lnrBackground.setBackgroundResource(R.drawable.att_late_icon)
            "P~/-" -> holder.lnrBackground.setBackgroundResource(R.drawable.l_and_n)


            "-/P" -> holder.lnrBackground.setBackgroundResource(R.drawable.n_and_p)
            "-/A" -> holder.lnrBackground.setBackgroundResource(R.drawable.n_and_a)
            "-/OD" -> holder.lnrBackground.setBackgroundResource(R.drawable.n_and_od)
            "-/P~" -> holder.lnrBackground.setBackgroundResource(R.drawable.n_and_l)
            "-/-" -> holder.lnrBackground.setBackgroundResource(R.drawable.att_no_taken)


            "OD/P" -> holder.lnrBackground.setBackgroundResource(R.drawable.od_and_p)
            "OD/A" -> holder.lnrBackground.setBackgroundResource(R.drawable.od_and_a)
            "OD/OD" -> holder.lnrBackground.setBackgroundResource(R.drawable.att_od_icon)
            "OD/P~" -> holder.lnrBackground.setBackgroundResource(R.drawable.od_and_l)
            "OD/-" -> holder.lnrBackground.setBackgroundResource(R.drawable.od_and_n)

            "H"->holder.lnrBackground.setBackgroundResource(R.drawable.holiday_icon)

            else -> holder.lnrBackground.setBackgroundResource(R.drawable.not_taken_icon)
        }



//        if (status == Constant.x || status == Constant.X_) {
//            holder.lnrBackground.setBackgroundResource(R.drawable.present_icon)
//        } else if (status == Constant.school) {
//            holder.lnrBackground.setBackgroundResource(R.drawable.absent_icon)
//        } else if (status == Constant.iffin) {
//            holder.lnrBackground.setBackgroundResource(R.drawable.not_taken_icon)
//        } else if (status == Constant.slash) {
//            holder.lnrBackground.setBackgroundResource(R.drawable.first_half_icon)
//        } else if (status == Constant.secondHalf) {
//            holder.lnrBackground.setBackgroundResource(R.drawable.second_half_icon)
//        } else if (status == Constant.section) {
//            holder.lnrBackground.setBackgroundResource(R.drawable.holiday_icon)
//        }

    }

    override fun getItemCount(): Int = items.size
}
