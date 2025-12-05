package com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingAdapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.StudentAttendanceReportData
import com.vs.schoolmessenger.Utils.ShimmerUtil

class AttendanceStudentReportAdapter(
    private var itemList: List<StudentAttendanceReportData>? = emptyList(),
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
                ShimmerUtil.wrapWithShimmer(parent, R.layout.attendance_student_report_item)
            ShimmerViewHolder(
                shimmerView
            )
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
        private val tvStudentName: TextView = itemView.findViewById(R.id.tvStudentName)
        private val tvAdmissionNo: TextView = itemView.findViewById(R.id.tvAdmissionNo)
        private val tvRollNo: TextView = itemView.findViewById(R.id.tvRollNo)

        //        private val tvStatus1: TextView = itemView.findViewById(R.id.tvStatus1)
        private val statusFN: TextView = itemView.findViewById(R.id.statusFN)
        private val statusAN: TextView = itemView.findViewById(R.id.statusAN)
//        private val imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatar)
//        private val tvStatus: LinearLayout = itemView.findViewById(R.id.tvStatus)

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: StudentAttendanceReportData, position: Int) {
            tvStudentName.text = data.student_name

            if (data.admission_no.isEmpty()) {
                tvAdmissionNo.visibility = View.GONE
            } else {
                tvAdmissionNo.visibility = View.VISIBLE
                tvAdmissionNo.text =
                    "${context.getString(R.string.admission_no)}: ${data.admission_no}"
            }

            if (data.roll_no.isEmpty()) {
                tvRollNo.visibility = View.GONE
            } else {
                tvRollNo.visibility = View.VISIBLE
                tvRollNo.text = "${context.getString(R.string.roll_no)}${data.roll_no}"
            }

            // 🔹 Split att_status like "P/P" or "A/P~"
            val statusParts = data.att_status.split("/")
            val fnStatus = statusParts.getOrNull(0)?.trim() ?: "-"
            val anStatus = statusParts.getOrNull(1)?.trim() ?: "-"


            setStatusView(statusFN, fnStatus)
            setStatusView(statusAN, anStatus)


        }

        private fun setStatusView(view: TextView, status: String) {
            val drawableRes = when (status.uppercase()) {
                "P" -> R.drawable.report_present_icon
                "A" -> R.drawable.report_absent_icon
                "P~" -> R.drawable.report_latercomer_icon
                "OD" -> R.drawable.report_od_icon
                else -> R.drawable.report_nottaken_icon // or "-"
            }

            // Set background drawable
            view.background = ContextCompat.getDrawable(context, drawableRes)

            // set the status text (P, A, etc.)
            view.text = if (status == "-") "-"
            else if (status == "P~") "LA"
            else status

        }
    }


    fun updateData(newList: List<StudentAttendanceReportData>) {
        itemList = newList
        notifyDataSetChanged()
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}