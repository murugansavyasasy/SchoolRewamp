package com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.Adapter.PreviewStaffLeaveRequest

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.Model.StaffLeaveHistory.GetStaffLeaveHistory
import com.vs.schoolmessenger.School.LeaveRequests.Listener.SchoolLRClickListener
import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class StaffLeaveHistory(
    private var itemList: List<GetStaffLeaveHistory>?,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var fullList: List<GetStaffLeaveHistory> = itemList ?: listOf()
    private var filteredList: List<GetStaffLeaveHistory> = itemList ?: listOf()

    init {
        fullList = itemList ?: listOf()
        filteredList = fullList
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.staff_leave_request_history)
            ShimmerViewHolder(shimmerView)
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.staff_leave_request_history, parent, false)
            DataViewHolder(view, context)
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


    fun updateData(newList: List<GetStaffLeaveHistory>) {
        this.fullList = newList
        notifyDataSetChanged()
    }


    class DataViewHolder(
        itemView: View, private val context: Context
    ) :
        RecyclerView.ViewHolder(itemView) {

        private val lblDay: TextView = itemView.findViewById(R.id.lblDay)
        private val lblMonth: TextView = itemView.findViewById(R.id.lblMonth)
        private val lblLeaveType: TextView = itemView.findViewById(R.id.lblLeaveType)
        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        private val lblLeaveStatus: TextView = itemView.findViewById(R.id.lblLeaveStatus)



        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: GetStaffLeaveHistory, position: Int) {

           val StartDate=Constant.isFormatDate(data.leave_from ?: "")
            val EndDate=Constant.isFormatDate(data.leave_to ?: "")
            val dayOrDays= if (data.no_of_days == Constant.one) context.getString(R.string.Day) else context.getString(
                    R.string.days
                )
            val (day, month) = Constant.getDayAndMonth(data.applied_on)
            lblDay.text = day
            lblMonth.text = month

            lblDate.text="${data.no_of_days} ${dayOrDays} • ${StartDate} - ${EndDate}"

            val bgDrawable = lblLeaveStatus.background as GradientDrawable

            when(data.status){

                Constant.rejected -> {

                    lblLeaveStatus.text = context.getString(R.string.rejected)
                    lblLeaveStatus.setTextColor(context.getColor(R.color.red))

                    bgDrawable.setColor(context.getColor(R.color.light_red))
                }

                Constant.approved -> {

                    lblLeaveStatus.text = context.getString(R.string.approved)
                    lblLeaveStatus.setTextColor(context.getColor(R.color.dark_green_4))

                    bgDrawable.setColor(context.getColor(R.color.light_green_6))
                }

                Constant.waiting_for_approval -> {

                    lblLeaveStatus.text = context.getString(R.string.pending)
                    lblLeaveStatus.setTextColor(context.getColor(R.color.dark_orange_6))

                    bgDrawable.setColor(context.getColor(R.color.light_orange_5))
                }
            }

            if (data.leave_type == "") {
                lblLeaveType.visibility = View.GONE
            } else {
                lblLeaveType.visibility = View.VISIBLE
                lblLeaveType.text = data.leave_type
            }
        }




    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}


