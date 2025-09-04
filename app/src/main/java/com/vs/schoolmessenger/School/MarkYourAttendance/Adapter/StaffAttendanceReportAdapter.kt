package com.vs.schoolmessenger.School.MarkYourAttendance.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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


        private val lblName: TextView = itemView.findViewById(R.id.lblName)
        private val lblRole: TextView = itemView.findViewById(R.id.lblRole)
        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        private val lblDay: TextView = itemView.findViewById(R.id.lblDay)
        private val lblCheckInTime: TextView = itemView.findViewById(R.id.lblCheckInTime)
        private val lblCheckOutTime: TextView = itemView.findViewById(R.id.lblCheckOutTime)
        private val lblHours: TextView = itemView.findViewById(R.id.lblHours)
        private val lnrParentCard: LinearLayout = itemView.findViewById(R.id.lnrParentCard)
        private val lnrAbsent: LinearLayout = itemView.findViewById(R.id.lnrAbsent)
        private val lnrPresent: LinearLayout = itemView.findViewById(R.id.lnrPresent)
        private val lblAbsentKey: TextView = itemView.findViewById(R.id.lblAbsentKey)
        private val lblAbsentValue: TextView = itemView.findViewById(R.id.lblAbsentValue)
        private val lblPresentKey: TextView = itemView.findViewById(R.id.lblPresentKey)
        private val lblPresentValue: TextView = itemView.findViewById(R.id.lblPresentValue)


        fun bind(
            data: StaffAttendanceReportData,
            position: Int,
            listener: AttendanceReportClickListener,
            adapter: StaffAttendanceReportAdapter
        ) {

            val attendanceMap = data.attendance_type
            attendanceMap.forEach { (key, value) ->
                if (value == Constant.Absent) {
                    lnrAbsent.visibility = View.VISIBLE
                    lnrPresent.visibility = View.GONE
                    lblAbsentKey.text = key
                    lblAbsentValue.text = value

//                    if (data.attendance_type.size != 2) {
//                        imgPunchHistory.visibility = View.GONE
//                    } else {
//                        imgPunchHistory.visibility = View.VISIBLE
//                    }
                }
                if (value == Constant.Present) {
                    lnrAbsent.visibility = View.GONE
                    lnrPresent.visibility = View.VISIBLE
                    lblPresentKey.text = key
                    lblPresentValue.text = value

//                    imgPunchHistory.visibility = View.VISIBLE
                }
            }


            lblName.text = data.name

            if (data.in_time != "") {
                lblCheckInTime.text = data.in_time

            } else {
                lblCheckInTime.text = "-"

            }
            if (data.out_time != "") {
                lblCheckOutTime.text = data.out_time
            } else {
                lblCheckOutTime.text = "-"
            }
            lblHours.text = data.working_hours
            lblRole.text = data.role

            val result = Constant.getDateDetails(data.date)
            lblDate.text = result.second.toString()
            lblDay.text = result.third

            lnrParentCard.setOnClickListener {
                attendanceMap.forEach { (_, value) ->
                    if (value == Constant.Present) {
                        listener.onItemClick(data)
                    } else if (value == Constant.Absent) {
                        Log.d("The user has no attendance history", "")
                    }
                }
            }
        }

        class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun startShimmer() {
                ShimmerUtil.startShimmer(itemView)
            }
        }
    }
}