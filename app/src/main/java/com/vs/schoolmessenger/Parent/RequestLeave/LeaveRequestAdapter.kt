package com.vs.schoolmessenger.Parent.RequestLeave

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class LeaveRequestAdapter(
    private var itemList: List<LeaveData>?,
    private var listener: LeaveRequestClickListener,
    private val context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var fullList: List<LeaveData> = itemList ?: listOf()
    private var filteredList: List<LeaveData> = fullList
    private var expandedPosition = RecyclerView.NO_POSITION


    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.leave_request_history_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.leave_request_history_item, parent, false)
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

    fun updateData(newList: List<LeaveData>) {
        fullList = newList
        filteredList = newList
        isLoading = false
        notifyDataSetChanged()
    }


    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textName: TextView = itemView.findViewById(R.id.textName)
        private val textDate: TextView = itemView.findViewById(R.id.textDate)
        private val textReason: TextView = itemView.findViewById(R.id.textReason)
        private val textNoOfDays: TextView = itemView.findViewById(R.id.textNoOfDays)
        private val textFirstLetter: TextView = itemView.findViewById(R.id.textFirstLetter)
        private val btnApprove: TextView = itemView.findViewById(R.id.btnApprove)
        private val textLeaveType: TextView = itemView.findViewById(R.id.textLeaveType)
        val options: ImageView = itemView.findViewById(R.id.options)
        private val relbuttons: RelativeLayout = itemView.findViewById(R.id.relbuttons)
        private val deleteButton: LinearLayout = itemView.findViewById(R.id.deletebutton)
        private val editButton: LinearLayout = itemView.findViewById(R.id.editbutton)
        private val lblGetOutPass: TextView = itemView.findViewById(R.id.lblGetOutPass)

        @SuppressLint("SetTextI18n")
        fun bind(
            data: LeaveData,
            listener: LeaveRequestClickListener,
            isExpanded: Boolean,
            context: Context,
        ) {
            textName.text = data.student_name
            textFirstLetter.text = data.student_name.firstOrNull()?.toString() ?: "?"

            textDate.text = "${Constant.convertDateTimeFormat2(data.leave_from ?: "")} - ${
                Constant.convertDateTimeFormat2(data.leave_to ?: "")
            }"
            textNoOfDays.text =
                "${data.no_of_days} ${
                    if (data.no_of_days == Constant.one) context.getString(R.string.Day) else context.getString(
                        R.string.days
                    )
                } ${context.getString(R.string.Application)}"
            textReason.text = data.reason

            if (data.leave_type == "") {
                textLeaveType.visibility = View.GONE
            } else {
                textLeaveType.visibility = View.VISIBLE
                textLeaveType.text = data.leave_type
            }


            when (data.status) {
                Constant.waiting_for_approval -> {
                    btnApprove.apply {
                        visibility = View.VISIBLE
                        text = context.getString(R.string.awaiting)
                        lblGetOutPass.visibility = View.GONE
                        applyTintedBackground(
                            btnApprove,
                            R.drawable.bg_leave_approved,
                            R.color.light_yellow_1
                        )
                        setTextColor(Color.parseColor("#996633"))
                        options.visibility = View.VISIBLE
                        relbuttons.visibility = View.GONE
                    }
                }

                Constant.approved -> {
                    btnApprove.apply {
                        visibility = View.VISIBLE
                        text = context.getString(R.string.approved)
                        lblGetOutPass.visibility = View.VISIBLE
                        applyTintedBackground(
                            btnApprove,
                            R.drawable.bg_leave_approved,
                            R.color.light_green_1
                        )
                        setTextColor(Color.parseColor("#2E7D32"))

                        options.visibility = View.GONE
                        relbuttons.visibility = View.GONE
                    }
                }

                Constant.rejected -> {
                    btnApprove.apply {
                        visibility = View.VISIBLE
                        text = context.getString(R.string.rejected)
                        lblGetOutPass.visibility = View.GONE
                        applyTintedBackground(
                            btnApprove,
                            R.drawable.bg_leave_approved,
                            R.color.light_red_1
                        )
                        setTextColor(Color.parseColor("#D32F2F"))
                        options.visibility = View.GONE
                        relbuttons.visibility = View.GONE
                    }
                }
            }
            relbuttons.visibility = if (isExpanded) View.VISIBLE else View.GONE



            deleteButton.setOnClickListener {
                listener.onItemDeleteClick(data)
            }

            editButton.setOnClickListener {
                listener.onItemEditClick(data)
            }

            lblGetOutPass.setOnClickListener {
                val context = it.context
                val myIntent = Intent(context, OutPass::class.java)
                val saveLeaveData = LeaveData(
                    id = data.id,
                    applied_on = data.applied_on,
                    student_name = data.student_name,
                    class_name = data.class_name,
                    section_name = data.section_name,
                    leave_from = data.leave_from,
                    leave_to = data.leave_to,
                    no_of_days = data.no_of_days,
                    reason = data.reason,
                    status = data.status,
                    updated_on = data.updated_on,
                    from_session = data.from_session,
                    to_session = data.to_session,
                    approved_by = data.approved_by,
                    leave_type = data.leave_type,
                    leave_type_id = data.leave_type_id,
                )
                Constant.isLeaveData = saveLeaveData
                context.startActivity(myIntent)
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