package com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.Adapter.ApproveStaffLeaveRequest

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.MarkAttendanceDataSending
import com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.Model.StaffLeaveRequestHistory.StaffLeaveData
import com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.Model.isStaffLeaveHistoryData
import com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.PreviewStaffLeaveRequest
import com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.listner.ApproveStaffLeaveRequestClickListener
import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.Constant.isStaffLeaveHistoryData
import com.vs.schoolmessenger.Utils.ShimmerUtil
import kotlin.String

class StaffApproveLeaveRequestAdapter(
    private var itemList: List<StaffLeaveData>?,
    private var listener: ApproveStaffLeaveRequestClickListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var fullList: List<StaffLeaveData> = itemList ?: listOf()
    private var filteredList: List<StaffLeaveData> = itemList ?: listOf()

    init {
        fullList = itemList ?: listOf()
        filteredList = fullList
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.staff_leave_request_list)
            ShimmerViewHolder(shimmerView)
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.staff_leave_request_list, parent, false)
            DataViewHolder(view, context, listener)
        }


    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(filteredList[position], position)

        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20
        else filteredList?.size ?: 0
    }


    fun updateData(newList: List<StaffLeaveData>) {
        this.fullList = newList
        notifyDataSetChanged()
    }


    class DataViewHolder(
        itemView: View, private val context: Context, private val listener: ApproveStaffLeaveRequestClickListener
    ) :
        RecyclerView.ViewHolder(itemView) {

        private val textName: TextView = itemView.findViewById(R.id.lblName)
        private val lblStartDate: TextView = itemView.findViewById(R.id.lblStartDate)
        private val lblEndDate: TextView = itemView.findViewById(R.id.lblEndDate)
        private val textReason: TextView = itemView.findViewById(R.id.Reason)
        private val textNoOfDays: TextView = itemView.findViewById(R.id.lblDays)
        private val btnCancel: TextView = itemView.findViewById(R.id.btnCancel)
        private val lblLogo: TextView = itemView.findViewById(R.id.lblLogo)
        private val lblLeaveStatus: TextView = itemView.findViewById(R.id.lblLeaveStatus)

        private val btnApprove: TextView = itemView.findViewById(R.id.btnApprove)
        private val textLeaveType: TextView = itemView.findViewById(R.id.lblLeaveType)
        private val lbltxtDays: TextView = itemView.findViewById(R.id.lbltxtDays)
        private val lblCallIcon: TextView = itemView.findViewById(R.id.lblCallIcon)
        private val lblSeeMoreDetails: TextView = itemView.findViewById(R.id.lblSeeMoreDetails)
        private val rlaHeader: RelativeLayout = itemView.findViewById(R.id.rlaHeader)
        private val cstStatus: ConstraintLayout = itemView.findViewById(R.id.cstStatus)


        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: StaffLeaveData, position: Int) {
            textName.text = data.staff_name
            lblLogo.text = Constant.getInitials(data.staff_name?:"")

            lblStartDate.text = Constant.convertDateTimeFormatDateMonth(data.from_date ?: "")

            lblEndDate.text = Constant.convertDateTimeFormatDateMonth(data.to_date ?: "")

            textNoOfDays.text = data.no_of_days.toString()

            lbltxtDays.text=
                if (data.no_of_days.toString() == Constant.one) context.getString(R.string.Day) else context.getString(
                    R.string.days
                )

            textReason.text = data.reason

            if (data.status == Constant.rejected) {
                applyTintedBackground(
                    lblLeaveStatus,
                    R.drawable.bg_leave_approved,
                    R.color.light_red_1
                )
                lblLeaveStatus.setTextColor(Color.parseColor("#D32F2F"))
                lblLeaveStatus.visibility= View.VISIBLE
                lblLeaveStatus.text=context.getString(R.string.rejected)
                cstStatus.visibility= View.GONE

            } else if (data.status == Constant.approved) {
                applyTintedBackground(
                    lblLeaveStatus,
                    R.drawable.bg_leave_approved,
                    R.color.light_green_1
                )
                lblLeaveStatus.setTextColor(Color.parseColor("#2E7D32"))
                lblLeaveStatus.visibility= View.VISIBLE
                lblLeaveStatus.text=context.getString(R.string.approved)
                cstStatus.visibility= View.GONE


            } else if (data.status == Constant.waiting_for_approval) {
                applyTintedBackground(
                    lblLeaveStatus,
                    R.drawable.bg_leave_approved,
                    R.color.light_yellow_1
                )
                lblLeaveStatus.setTextColor(Color.parseColor("#996633"))
                lblLeaveStatus.text=context.getString(R.string.pending)

                lblLeaveStatus.visibility= View.GONE
                cstStatus.visibility= View.GONE
                btnApprove.text=context.getString(R.string.approve)
                btnCancel.text=context.getString(R.string.reject)


            }

            if (data.leave_type == "") {
                textLeaveType.visibility = View.GONE
            } else {
                textLeaveType.visibility = View.VISIBLE
                textLeaveType.text = data.leave_type
            }


            lblCallIcon.setOnClickListener{
                Constant.redirectToDialPad(context,"0000000000")
            }



            val openExam = View.OnClickListener {
                val intent = Intent(context, PreviewStaffLeaveRequest::class.java)

                val isSelectedStaffLeaveHistoryData = isStaffLeaveHistoryData(
                    id=data.staff_id,
                    applied_on=data.applied_on,
                    staff_name=data.staff_name,
                    from_date=data.from_date,
                    to_date=data.to_date,
                    no_of_days=data.no_of_days,
                    status=data.status,
                    reason = data.reason,
                    updated_on=data.updated_on,
                    from_session=data.from_session,
                    to_session=data.to_session,
                    approved_by=data.approved_by,
                    leave_type=data.leave_type,
                    leave_type_id=data.leave_type_id,
                    status_id=data.status_id
                )
                //We are Saving all the data in Constant as List Here
                Constant.isStaffLeaveHistoryData = isSelectedStaffLeaveHistoryData
                Log.d("isSelectedStaffLeaveHistoryData", isSelectedStaffLeaveHistoryData.toString())
                context.startActivity(intent)
            }
            lblSeeMoreDetails.setOnClickListener(openExam)

            btnApprove.setOnClickListener {
                if (data.status.equals(Constant.waiting_for_approval)) {
                    listener.onApproveClicked(data, position, true) { isApproved ->
                        if (isApproved) {
                            data.status = Constant.approved
                            listener.onUpdateStatus(data)
                        }
                    }
                }
            }


            btnCancel.setOnClickListener {
                if (data.status.equals(Constant.waiting_for_approval)) {

                    listener.onApproveClicked(data, position, false) { isApproved ->
                        if (isApproved) {
                            data.status = Constant.rejected
                            listener.onUpdateStatus(data)
                        }
                    }
                }
            }


        }
        fun applyTintedBackground(view: View, drawableRes: Int, colorRes: Int) {
            val context = view.context
            val bgDrawable = ContextCompat.getDrawable(context, drawableRes)
            bgDrawable?.setTint(ContextCompat.getColor(context, colorRes))
            view.background = bgDrawable
        }




    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}