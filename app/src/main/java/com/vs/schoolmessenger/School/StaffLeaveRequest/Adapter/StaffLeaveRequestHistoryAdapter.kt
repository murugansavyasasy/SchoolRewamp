package com.vs.schoolmessenger.School.StaffLeaveRequest.Adapter


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.Model.StaffLeaveRequestHistory.StaffLeaveData
import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveData
import com.vs.schoolmessenger.School.StaffLeaveRequest.Listner.StaffLeaveRequestClickListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class StaffLeaveRequestHistoryAdapter(
    private var itemList: List<StaffLeaveData>?,
    private var listener: StaffLeaveRequestClickListener,
    private val context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var fullList: List<StaffLeaveData> = itemList ?: listOf()
    private var filteredList: List<StaffLeaveData> = fullList
    private var expandedPosition = RecyclerView.NO_POSITION


    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.staff_leave_history_request)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.staff_leave_history_request, parent, false)
            DataViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else filteredList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder && !isLoading) {
            val isExpanded = position == expandedPosition
            holder.bind(filteredList[position], listener, isExpanded, context)

            holder.options.setOnClickListener {
                if (expandedPosition == position) {
                    val prevPosition = expandedPosition
                    expandedPosition = RecyclerView.NO_POSITION
                    notifyItemChanged(prevPosition)
                } else {
                    val prevPosition = expandedPosition
                    expandedPosition = position
                    notifyItemChanged(prevPosition)
                    notifyItemChanged(position)
                }
            }

        }
    }

    fun updateData(newList: List<StaffLeaveData>) {
        fullList = newList
        filteredList = newList
        isLoading = false
        notifyDataSetChanged()
    }


    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textName: TextView = itemView.findViewById(R.id.lblName)
        private val lblStartDate: TextView = itemView.findViewById(R.id.lblStartDate)
        private val lblEndDate: TextView = itemView.findViewById(R.id.lblEndDate)
        private val textReason: TextView = itemView.findViewById(R.id.Reason)
        private val textNoOfDays: TextView = itemView.findViewById(R.id.lblDays)
        private val btnCancel: TextView = itemView.findViewById(R.id.btnCancel)
        private val lblLogo: TextView = itemView.findViewById(R.id.lblLogo)

        val options: ImageView = itemView.findViewById(R.id.options)

        private val btnApprove: TextView = itemView.findViewById(R.id.btnApprove)
        private val ConsStatusButton: ConstraintLayout = itemView.findViewById(R.id.ConsStatusButton)
        private val textLeaveType: TextView = itemView.findViewById(R.id.lblLeaveType)
        private val lbltxtDays: TextView = itemView.findViewById(R.id.lbltxtDays)
        private val lblLeaveStatus: TextView = itemView.findViewById(R.id.lblLeaveStatus)
        private val relbuttons: RelativeLayout = itemView.findViewById(R.id.relbuttons)
        private val deleteButton: LinearLayout = itemView.findViewById(R.id.deletebutton)
        private val editButton: LinearLayout = itemView.findViewById(R.id.editbutton)
        private val rlaHeader: FrameLayout = itemView.findViewById(R.id.rlaHeader)

        @SuppressLint("SetTextI18n")
        fun bind(
            data: StaffLeaveData,
            listener: StaffLeaveRequestClickListener,
            isExpanded: Boolean,
            context: Context,
        ) {
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
                options.visibility = View.GONE
                relbuttons.visibility = View.GONE
                lblLeaveStatus.text=context.getString(R.string.rejected)
                ConsStatusButton.visibility= View.GONE

            }
            else if (data.status == Constant.approved) {
                applyTintedBackground(
                    lblLeaveStatus,
                    R.drawable.bg_leave_approved,
                    R.color.light_green_1
                )
                lblLeaveStatus.setTextColor(Color.parseColor("#2E7D32"))

                lblLeaveStatus.visibility= View.VISIBLE
                lblLeaveStatus.text=context.getString(R.string.approved)
                ConsStatusButton.visibility= View.GONE
                btnCancel.visibility= View.GONE
                options.visibility = View.GONE
                relbuttons.visibility = View.GONE

            }
            else if (data.status == Constant.waiting_for_approval) {
                lblLeaveStatus.visibility= View.INVISIBLE
                ConsStatusButton.visibility= View.VISIBLE
                btnApprove.text=context.getString(R.string.approve)
                btnCancel.text=context.getString(R.string.reject)
            }

            if (data.leave_type == "") {
                textLeaveType.visibility = View.GONE
            } else {
                textLeaveType.visibility = View.VISIBLE
                textLeaveType.text = data.leave_type
            }

            relbuttons.visibility = if (isExpanded) View.VISIBLE else View.GONE

            deleteButton.setOnClickListener {
                listener.onItemDeleteClick(data)
            }

            editButton.setOnClickListener {
                listener.onItemEditClick(data)
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
        init {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}