package com.vs.schoolmessenger.School.AbsenteesMarking

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R

class AttendanceStudentReportAdapter(
    private var itemList: List<StudentAttendanceReportData>?,
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
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.shimmer_view_small_list, parent, false)
            ShimmerViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.attendance_student_report_item, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            holder.bind(itemList!![position], position)

        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val lblStudentName: TextView = itemView.findViewById(R.id.lblStudentName)
        private val lblAdmissionValue: TextView = itemView.findViewById(R.id.lblAdmissionValue)
        private val lblAttendanceStatus: TextView = itemView.findViewById(R.id.lblAttendanceStatus)
        private val rlaAttendance: RelativeLayout = itemView.findViewById(R.id.rlaAttendance)

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: StudentAttendanceReportData, position: Int) {
            lblStudentName.text = data.Name
            lblAdmissionValue.text = data.RollNo
            lblAttendanceStatus.text = data.Status

            if (data.Status == "Absent") {
                lblAttendanceStatus.setBackgroundDrawable(context.resources.getDrawable(R.drawable.rounded_top_right_bottom_end_red))
                rlaAttendance.setBackgroundDrawable(context.resources.getDrawable(R.drawable.bg_outline_red))
            } else {
                lblAttendanceStatus.setBackgroundDrawable(context.resources.getDrawable(R.drawable.rounded_top_right_bottom_end_green))
                rlaAttendance.setBackgroundDrawable(context.resources.getDrawable(R.drawable.bg_outline_green))
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmer_view_container)

        init {
            shimmerLayout.startShimmer() // Start shimmer effect
        }
    }
}