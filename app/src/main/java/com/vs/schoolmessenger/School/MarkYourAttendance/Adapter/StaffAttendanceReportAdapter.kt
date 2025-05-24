package com.vs.schoolmessenger.School.MarkYourAttendance.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Communication.Adapter.TextHistoryAdapter
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.StaffAttendanceReportData
import com.vs.schoolmessenger.School.MarkYourAttendance.Interface.AttendanceReportClickListener
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
        private val lblPresentStatus: TextView = itemView.findViewById(R.id.lblPresentStatus)
        private val lblAbsentStatus: TextView = itemView.findViewById(R.id.lblAbsentStatus)

        private val lblAbsentLabel: TextView = itemView.findViewById(R.id.lblAbsentLabel)
        private val lblPresentLabel: TextView = itemView.findViewById(R.id.lblPresentLabel)
        private val lblStaffName: TextView = itemView.findViewById(R.id.lblStaffName)
        private val lblCheckInTime: TextView = itemView.findViewById(R.id.lblCheckInTime)
        private val lblCheckoutTime: TextView = itemView.findViewById(R.id.lblCheckoutTime)
        private val lblWorkingHours: TextView = itemView.findViewById(R.id.lblWorkingHours)
        private val lblMonth: TextView = itemView.findViewById(R.id.lblMonth)
        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        private val lblDay: TextView = itemView.findViewById(R.id.lblDay)
        private val rytParentCard: RelativeLayout = itemView.findViewById(R.id.rytParentCard)
        private val lblStaffDesignation: TextView = itemView.findViewById(R.id.lblStaffDesignation)
        private val lnrDate: LinearLayout = itemView.findViewById(R.id.lnrDate)

        fun bind(
            data: StaffAttendanceReportData,
            position: Int,
            listener: AttendanceReportClickListener,
            adapter: StaffAttendanceReportAdapter
        ) {

            val attendanceMap = data.attendance_type
            lblPresentLabel.visibility = View.GONE
            lblAbsentLabel.visibility = View.GONE
            lblPresentStatus.visibility = View.GONE
            lblAbsentStatus.visibility = View.GONE

            attendanceMap.forEach { (key, value) ->
                if (value == Constant.Present) {
                    lblPresentLabel.visibility = View.VISIBLE
                    lblPresentStatus.visibility = View.VISIBLE
                    lblPresentLabel.text = key
                    lblPresentStatus.text = value
                } else if (value == Constant.Absent) {
                    lblAbsentLabel.visibility = View.VISIBLE
                    lblAbsentStatus.visibility = View.VISIBLE
                    lblAbsentLabel.text = key
                    lblAbsentStatus.text = value
                }
            }

            lblStaffName.text = data.name
            lblCheckInTime.text = context.getString(R.string.Firstin) + data.in_time
            if (data.out_time != "") {
                lblCheckoutTime.visibility = View.VISIBLE
                lblCheckoutTime.text = context.getString(R.string.Lastin) + data.out_time
            } else {
                lblCheckoutTime.visibility = View.GONE
            }
            lblWorkingHours.text = context.getString(R.string.Workinghours) + data.working_hours
            lblStaffDesignation.text = data.role

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