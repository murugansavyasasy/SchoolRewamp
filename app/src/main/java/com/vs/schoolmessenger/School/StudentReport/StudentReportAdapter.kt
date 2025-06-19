package com.vs.schoolmessenger.School.StudentReport

import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil

class StudentReportAdapter(
    private var itemList: List<StudentReportData>? = emptyList(),
    private var listener: StudentReportClickListener,
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
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.student_report_item)
            ShimmerViewHolder(
                shimmerView
            )

        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.student_report_item, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            itemList?.get(position)?.let { data ->
                holder.bind(data, listener)
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val lblAdmissionNumber: TextView = itemView.findViewById(R.id.lblAdmisionNumber)
        private val lblGender: TextView = itemView.findViewById(R.id.lblGender)
        private val lblDOB: TextView = itemView.findViewById(R.id.lblDOB)
        private val lblStudentName: TextView = itemView.findViewById(R.id.lblStudentName)
        private val lblFatherName: TextView = itemView.findViewById(R.id.lblFatherName)
        private val lblTeacherName: TextView = itemView.findViewById(R.id.lblTeacherName)
        private val lblStandardAndSection: TextView = itemView.findViewById(R.id.lblStandardAndSection)
        private val profileImage: ImageView = itemView.findViewById(R.id.imgStudent)
        private val lnrPhoneNumber: LinearLayout = itemView.findViewById(R.id.lnrMobileNumber)
        private val lnrSms: LinearLayout = itemView.findViewById(R.id.lnrSMS)
        private val lnrMail: LinearLayout = itemView.findViewById(R.id.lnrEmail)

        fun bind(data: StudentReportData, listener: StudentReportClickListener) {
            // Bind actual data to the views
            lblAdmissionNumber.text = data.admission_no
            lblGender.text = data.gender
            lblDOB.text = data.dob
            lblStudentName.text = data.name
            lblFatherName.text = data.father_name
            lblTeacherName.text = data.class_teacher
            lblStandardAndSection.text = data.class_name +"-"+data.section_name
            if(data.primary_mobile!=""){
                lnrPhoneNumber.visibility=View.VISIBLE
                lnrSms.visibility=View.VISIBLE

            }else{
                lnrPhoneNumber.visibility=View.GONE
                lnrSms.visibility=View.GONE
            }
            if(data.email!=""){
                lnrMail.visibility=View.VISIBLE
            }else{
                lnrMail.visibility=View.GONE
            }
            Glide.with(context)
                .load(data.profile)
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.default_profile)
                .into(profileImage);


            lnrMail.setOnClickListener {
                listener.onMailClick(data)
            }

            lnrSms.setOnClickListener {
                listener.onMessageClick(data)
            }

            lnrPhoneNumber.setOnClickListener {
                listener.onPhoneClick(data)
            }
        }

    }

    fun updateData(newList: List<StudentReportData>) {
        itemList = newList
        notifyDataSetChanged()
    }


    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }

}
