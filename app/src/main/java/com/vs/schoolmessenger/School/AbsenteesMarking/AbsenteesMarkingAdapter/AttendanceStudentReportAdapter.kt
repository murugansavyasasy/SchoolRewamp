package com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingAdapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.StudentAttendanceReportData
import com.vs.schoolmessenger.Utils.Constant
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
        private val lblName: TextView = itemView.findViewById(R.id.tvName)
        private val lblAdmissionNoValue: TextView = itemView.findViewById(R.id.lblAdmissionNoValue)
        private val tvRollNo: TextView = itemView.findViewById(R.id.tvRollNo)
        private val tvStatus1: TextView = itemView.findViewById(R.id.tvStatus1)
        private val imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatar)
        private val tvStatus: LinearLayout = itemView.findViewById(R.id.tvStatus)

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: StudentAttendanceReportData, position: Int) {
            lblName.text = data.student_name

            if (data.admission_no.isEmpty()){
                lblAdmissionNoValue.visibility=View.GONE
            }
            else{
                lblAdmissionNoValue.visibility=View.VISIBLE
                lblAdmissionNoValue.text = data.admission_no
            }

            if (data.roll_no.isEmpty()){
                tvRollNo.visibility=View.GONE
            }
            else{
                tvRollNo.visibility=View.VISIBLE
                tvRollNo.text = data.roll_no
            }

            if (data.att_status == Constant.P){
                tvStatus1.text =context.getString(R.string.present)
                tvStatus.background.setTint(ContextCompat.getColor(context, R.color.green))
                tvStatus1.setTextColor(ContextCompat.getColor(context,R.color.white))

            }
            else{
                tvStatus1.text =context.getString(R.string.absent)
                tvStatus.background.setTint(ContextCompat.getColor(context,R.color.red))
                tvStatus1.setTextColor(ContextCompat.getColor(context,R.color.white))
            }

            val profileUrl = data.profile

            val defaultAvatar = when {
                data.gender.equals(Constant.male, ignoreCase = true) -> R.drawable.avatar
                data.gender.equals(Constant.female, ignoreCase = true) -> R.drawable.girl_avatar
                else -> R.drawable.person_circle // fallback if gender is unknown
            }

            if (profileUrl.isNullOrEmpty()) {
                // No profile URL → load gender-based default directly
                Glide.with(imgAvatar.context)
                    .load(defaultAvatar)
                    .placeholder(R.drawable.person_circle)
                    .into(imgAvatar)
            } else {
                // Load URL → if fails, fallback to gender-based drawable
                Glide.with(imgAvatar.context)
                    .load(profileUrl)
                    .placeholder(R.drawable.person_circle)
                    .error(defaultAvatar)
                    .into(imgAvatar)
            }

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