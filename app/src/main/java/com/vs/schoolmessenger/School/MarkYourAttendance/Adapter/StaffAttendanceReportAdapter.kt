package com.vs.schoolmessenger.School.MarkYourAttendance.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Communication.TextHistoryAdapter
import com.vs.schoolmessenger.School.MarkYourAttendance.Interface.AttendanceReportClickListener
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.StaffAttendanceReportData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class StaffAttendanceReportAdapter(
    private var itemList: List<StaffAttendanceReportData>?,
    private var listener: AttendanceReportClickListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.giometric_attendance_report_item_list)
            TextHistoryAdapter.DataViewHolder.ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.giometric_attendance_report_item_list, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList!![position], position, listener, this) // Pass adapter reference
        } else if (holder is TextHistoryAdapter.DataViewHolder.ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val lblStatus: TextView = itemView.findViewById(R.id.lblStatus)
        private val lblStaffName: TextView = itemView.findViewById(R.id.lblStaffName)
        private val lblCheckInTime: TextView = itemView.findViewById(R.id.lblCheckInTime)
        private val lblCheckoutTime: TextView = itemView.findViewById(R.id.lblCheckoutTime)
        private val lblWorkingHours: TextView = itemView.findViewById(R.id.lblWorkingHours)
        private val lblMonth: TextView = itemView.findViewById(R.id.lblMonth)
        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        private val lblDay: TextView = itemView.findViewById(R.id.lblDay)
        private val rytParentCard: RelativeLayout = itemView.findViewById(R.id.rytParentCard)
        private val lblAttendanceType: TextView = itemView.findViewById(R.id.lblAttendanceType)

        fun bind(
            data: StaffAttendanceReportData,
            position: Int,
            listener: AttendanceReportClickListener,
            adapter: StaffAttendanceReportAdapter
        ) {
            lblStatus.text = data.leave_type

            if (data.leave_type.equals("Present")){
                lblStatus.background = ContextCompat.getDrawable(context, R.drawable.rounded_top_right_bottom_end_green)
            }else{
                lblStatus.background = ContextCompat.getDrawable(context, R.drawable.rounded_top_right_bottom_end_red)
            }

            lblStaffName.text = data.name
            lblCheckInTime.text = "Check in time : " + data.in_time
            lblCheckoutTime.text = "Check out time " + data.out_time
            lblWorkingHours.text = "Working hours " + data.working_hours
            lblAttendanceType.text = data.attendance_type

            val result = Constant.getDateDetails(data.date)
            lblMonth.text = result.first
            lblDate.text = result.second.toString()
            lblDay.text = result.third

            rytParentCard.setOnClickListener {
                listener.onItemClick(data)
            }
        }

        class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun startShimmer() {
                ShimmerUtil.startShimmer(itemView)
            }
        }
    }
}