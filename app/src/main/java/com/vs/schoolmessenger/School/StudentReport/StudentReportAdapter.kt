package com.vs.schoolmessenger.School.StudentReport

import android.content.Context
import android.graphics.Paint
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
            com.vs.schoolmessenger.Parent.Communication.UnifiedVoiceAdapter.ShimmerViewHolder(
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
        private val lblMobileNumber: TextView = itemView.findViewById(R.id.lblMobileNumber)
        private val lblStandard: TextView = itemView.findViewById(R.id.lblStandard)
        private val lblSection: TextView = itemView.findViewById(R.id.lblSection)
        private val profileImage: ImageView = itemView.findViewById(R.id.imgStudent)
        private val lblEmail: TextView = itemView.findViewById(R.id.lblEmail)
        private val lnrPhoneNumber: LinearLayout = itemView.findViewById(R.id.lnrPhoneNumber)
        private val lnrSms: LinearLayout = itemView.findViewById(R.id.lnrSms)
        private val lnrMail: LinearLayout = itemView.findViewById(R.id.lnrMail)

        fun bind(data: StudentReportData, listener: StudentReportClickListener) {
            // Bind actual data to the views
            lblAdmissionNumber.text = data.admission_no
            lblGender.text = data.gender
            lblDOB.text = data.dob
            lblStudentName.text = data.name
            lblFatherName.text = data.father_name
            lblTeacherName.text = data.class_teacher
            lblStandard.text = data.class_name
            lblSection.text = data.section_name
            if(data.primary_mobile!=""){
                lnrPhoneNumber.visibility=View.VISIBLE
                lnrSms.visibility=View.VISIBLE
                lblMobileNumber.text = data.primary_mobile
                lblMobileNumber.setPaintFlags(lblMobileNumber.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)

            }else{
                lnrPhoneNumber.visibility=View.GONE
                lnrSms.visibility=View.GONE
            }
            if(data.email!=""){
                lnrMail.visibility=View.VISIBLE
                lblEmail.text = data.email
                lblEmail.setPaintFlags(lblEmail.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)

            }else{
                lnrMail.visibility=View.GONE
            }
            Glide.with(context)
                .load(data.profile)
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.default_profile)
                .into(profileImage);


            // Set click listener for the email TextView
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
    enum class SortType {
        NO_ASC,
        NO_DESC,
        NAME_ASC,
        NAME_DESC
    }
    fun sortData(sortType: SortType) {
        val sortedList = when (sortType) {
            SortType.NO_ASC -> itemList?.sortedBy { it.admission_no }
            SortType.NO_DESC -> itemList?.sortedByDescending { it.admission_no }
            SortType.NAME_ASC -> itemList?.sortedBy { it.name }
            SortType.NAME_DESC -> itemList?.sortedByDescending { it.name }
        }

        updateData(sortedList ?: emptyList())
    }


    fun updateData(newList: List<StudentReportData>) {
        itemList = newList
        notifyDataSetChanged()
    }


    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout = itemView.findViewById(R.id.shimmer_view_container)

        init {
            shimmerLayout.startShimmer() // Start shimmer effect
        }
    }

}
