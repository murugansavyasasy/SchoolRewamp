package com.vs.schoolmessenger.School.AttendanceReportFromStaff

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R

class AttendanceChildAdapter(
    private val list: List<AttendanceDetailDataClass>,
    private val listener: OnAttendanceHistoryClickListener
) : RecyclerView.Adapter<AttendanceChildAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val lblName: TextView = itemView.findViewById(R.id.lblName)
        val lblRole: TextView = itemView.findViewById(R.id.lblRole)

        val lnrAbsent: LinearLayout = itemView.findViewById(R.id.lnrAbsent)
        val lnrPresent: LinearLayout = itemView.findViewById(R.id.lnrPresent)
        val lnrParentCard: LinearLayout = itemView.findViewById(R.id.lnrParentCard)
        val lnrDate: LinearLayout = itemView.findViewById(R.id.lnrDate)

        val lblAbsentKey: TextView = itemView.findViewById(R.id.lblAbsentKey)
        val lblAbsentValue: TextView = itemView.findViewById(R.id.lblAbsentValue)

        val lblPresentKey: TextView = itemView.findViewById(R.id.lblPresentKey)
        val lblPresentValue: TextView = itemView.findViewById(R.id.lblPresentValue)

        val lblCheckInTime: TextView = itemView.findViewById(R.id.lblCheckInTime)
        val lblCheckOutTime: TextView = itemView.findViewById(R.id.lblCheckOutTime)
        val lblHours: TextView = itemView.findViewById(R.id.lblHours)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.giometric_attendance_report_item_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]

        holder.lblName.text = item.name
        holder.lblRole.text = item.role

        holder.lnrDate.visibility= View.GONE

        holder.lblCheckInTime.text = item.in_time
        holder.lblCheckOutTime.text = item.out_time
        holder.lblHours.text = item.working_hours + " hrs"

        val entry = item.attendance_type.entries.firstOrNull()
        val key = entry?.key ?: ""
        val value = entry?.value ?: ""

        if (value.equals("Present", true)) {

            holder.lnrPresent.visibility = View.VISIBLE
            holder.lnrAbsent.visibility = View.GONE

            holder.lblPresentKey.text = key
            holder.lblPresentValue.text = value

            holder.lnrParentCard.setOnClickListener {
                listener.onAttendanceClick(item)
            }

        } else {

            holder.lnrPresent.visibility = View.GONE
            holder.lnrAbsent.visibility = View.VISIBLE

            holder.lblAbsentKey.text = key
            holder.lblAbsentValue.text = value
        }
    }
}