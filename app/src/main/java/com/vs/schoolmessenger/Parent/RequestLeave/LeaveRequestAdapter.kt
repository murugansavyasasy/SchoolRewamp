package com.vs.schoolmessenger.Parent.RequestLeave

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.LeaveRequests.LeaveRequestAdapter.ShimmerViewHolder
import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class LeaveRequestAdapter(
    private var itemList: List<LeaveData>?,
    private var listener: LeaveRequestClickListener,
    private var context: Context,
    private var isLoading: Boolean

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var fullList: List<LeaveData> = itemList ?: listOf()
    private var filteredList: List<LeaveData> = fullList


    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {

            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.leave_request_history_item)
            ShimmerViewHolder(
                shimmerView
            )
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.leave_request_history_item, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else filteredList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(filteredList[position], position, listener, this)
        }
    }

    fun updateData(newList: List<LeaveData>) {
        fullList = newList
        filteredList = newList
        notifyDataSetChanged()
    }

    fun filterByStatus(status: String) {
        filteredList = if (status == "All") {
            fullList
        } else {
            fullList.filter { it.status.equals(status, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private var isTextExpanded = false

        private val textName: TextView = itemView.findViewById(R.id.textName)
        private val textDate: TextView = itemView.findViewById(R.id.textDate)
        private val textReason: TextView = itemView.findViewById(R.id.textReason)
        private val textNoOfDays: TextView = itemView.findViewById(R.id.textNoOfDays)
        private val textFirstLetter: TextView = itemView.findViewById(R.id.textFirstLetter)
        private val btnApprove: TextView = itemView.findViewById(R.id.btnApprove)
        private val options: ImageView = itemView.findViewById(R.id.options)
        private val relbuttons: RelativeLayout = itemView.findViewById(R.id.relbuttons)
        private val deletebutton: LinearLayout = itemView.findViewById(R.id.deletebutton)
        private val editbutton: LinearLayout = itemView.findViewById(R.id.editbutton)

        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            data: LeaveData,
            position: Int,
            listener: LeaveRequestClickListener,
            adapter: LeaveRequestAdapter
        ) {
            textName.text = data.student_name
            textFirstLetter.text = data.student_name.first().toString()
            textDate.text = Constant.convertDateTimeFormat(data.leave_from.toString()) +
                    " - " + Constant.convertDateTimeFormat(data.leave_to.toString())

            textNoOfDays.text = if (data.no_of_days == "1") {
                "( ${data.no_of_days} Day )"
            } else {
                "( ${data.no_of_days} Days )"
            }

            textReason.text = data.reason

            when (data.status) {
                Constant.waiting_for_approval -> {
                    btnApprove.visibility = View.VISIBLE
                    btnApprove.setBackgroundResource(R.drawable.bg_leave_waiting)
                    btnApprove.text = "Waiting"
                    options.visibility = View.VISIBLE
                    relbuttons.visibility = View.GONE
                }

                Constant.approved -> {
                    btnApprove.visibility = View.VISIBLE
                    btnApprove.setBackgroundResource(R.drawable.bg_leave_approved)
                    btnApprove.text = "Approved"
                    options.visibility = View.GONE
                    relbuttons.visibility = View.GONE
                }

                Constant.rejected -> {
                    btnApprove.visibility = View.VISIBLE
                    btnApprove.setBackgroundResource(R.drawable.bg_leave_rejected)
                    btnApprove.text = "Rejected"
                    options.visibility = View.GONE
                    relbuttons.visibility = View.GONE
                }
            }

            options.setOnClickListener {
                if (relbuttons.visibility == View.VISIBLE) {
                    relbuttons.visibility = View.GONE
                    btnApprove.visibility = View.VISIBLE
                } else {
                    relbuttons.visibility = View.VISIBLE
                    btnApprove.visibility = View.GONE
                }
            }


            deletebutton.setOnClickListener {
                listener.onItemDeleteClick(data)
            }

            editbutton.setOnClickListener {
                listener.onItemEditClick(data)
            }

        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}