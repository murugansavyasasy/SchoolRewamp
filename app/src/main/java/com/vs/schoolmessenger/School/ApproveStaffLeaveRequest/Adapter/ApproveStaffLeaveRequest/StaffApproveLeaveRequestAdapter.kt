package com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.Adapter.ApproveStaffLeaveRequest

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.PreviewStaffLeaveRequest
import com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.listner.ApproveStaffLeaveRequestClickListener
import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class StaffApproveLeaveRequestAdapter(
    private var itemList: List<LeaveData>?,
    private var listener: ApproveStaffLeaveRequestClickListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var fullList: List<LeaveData> = itemList ?: listOf()
    private var filteredList: List<LeaveData> = itemList ?: listOf()

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


    fun updateData(newList: List<LeaveData>) {
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

        private val btnApprove: TextView = itemView.findViewById(R.id.btnApprove)
        private val textLeaveType: TextView = itemView.findViewById(R.id.lblLeaveType)
        private val lbltxtDays: TextView = itemView.findViewById(R.id.lbltxtDays)
        private val rlaHeader: RelativeLayout = itemView.findViewById(R.id.rlaHeader)


        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: LeaveData, position: Int) {
            textName.text = data.student_name
            lblLogo.text = Constant.getInitials(data.student_name)

            lblStartDate.text = Constant.convertDateTimeFormatDateMonth(data.leave_from ?: "")

            lblEndDate.text = Constant.convertDateTimeFormatDateMonth(data.leave_to ?: "")

            textNoOfDays.text = data.no_of_days

            lbltxtDays.text=
                if (data.no_of_days == Constant.one) context.getString(R.string.Day) else context.getString(
                    R.string.days
                )

            textReason.text = data.reason

            if (data.status == Constant.rejected) {
                btnCancel.visibility= View.VISIBLE
                btnCancel.text=context.getString(R.string.rejected)
                btnApprove.visibility= View.GONE


            } else if (data.status == Constant.approved) {
                btnCancel.visibility= View.GONE
                btnApprove.visibility= View.VISIBLE
                btnApprove.text=context.getString(R.string.approved)



            } else if (data.status == Constant.waiting_for_approval) {
                btnApprove.text=context.getString(R.string.approve)
                btnCancel.text=context.getString(R.string.reject)
                btnCancel.visibility= View.VISIBLE
                btnApprove.visibility= View.VISIBLE
            }

            if (data.leave_type == "") {
                textLeaveType.visibility = View.GONE
            } else {
                textLeaveType.visibility = View.VISIBLE
                textLeaveType.text = data.leave_type
            }


            val openExam = View.OnClickListener {
                val intent = Intent(context, PreviewStaffLeaveRequest::class.java)
                intent.putExtra(Constant.isStaffName,"SARANRAJ")
                intent.putExtra(Constant.isStaffSubjectName, "English")
//                intent.putExtra(Constant.isQuizScreenRole, true)
                context.startActivity(intent)
            }
            rlaHeader.setOnClickListener(openExam)

//            btnApprove.setOnClickListener {
//                if (data.status.equals(Constant.waiting_for_approval)) {
//                    listener.onApproveClicked(data, position, true) { isApproved ->
//                        if (isApproved) {
//                            data.status = Constant.approved
//                            listener.onUpdateStatus(data)
//                        }
//                    }
//                }
//            }


//            btnCancel.setOnClickListener {
//                if (data.status.equals(Constant.waiting_for_approval)) {
//
//                    listener.onApproveClicked(data, position, false) { isApproved ->
//                        if (isApproved) {
//                            data.status = Constant.rejected
//                            listener.onUpdateStatus(data)
//                        }
//                    }
//                }
//            }

        }




    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}