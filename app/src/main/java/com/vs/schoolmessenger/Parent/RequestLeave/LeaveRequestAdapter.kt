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

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {

            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.leave_request_history_item)
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            holder.bind(itemList!![position], position, listener, this)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private var isTextExpanded = false

        private val textName: TextView = itemView.findViewById(R.id.textName)
        private val textDate: TextView = itemView.findViewById(R.id.textDate)
        private val textReason: TextView = itemView.findViewById(R.id.textReason)
        private val textNoOfDays: TextView = itemView.findViewById(R.id.textNoOfDays)
        private val textFirstLetter: TextView = itemView.findViewById(R.id.textFirstLetter)
        private val btnCancel: TextView = itemView.findViewById(R.id.btnCancel)
        private val btnApprove: TextView = itemView.findViewById(R.id.btnApprove)


        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            data: LeaveData,
            position: Int,
            listener: LeaveRequestClickListener,
            adapter: LeaveRequestAdapter
        ) {
            textName.text = data.student_name
            textFirstLetter.text = data.student_name.first().toString()
//            lblSection.text = data.class_name
            textDate.text =  Constant.convertDateTimeFormat(data.leave_from.toString()) + " - "+Constant.convertDateTimeFormat(data.leave_to.toString())
            if(data.no_of_days.equals("1")){
                textNoOfDays.text = "( "+data.no_of_days+" Day )"
            }
            else{
                textNoOfDays.text = "( "+data.no_of_days+" Days )"
            }
            // lbldate.text =data.applied_on
            textReason.text = data.reason
            when (data.status) {

                Constant.waiting_for_approval -> {
                    btnCancel.visibility = View.GONE
                    btnApprove.visibility = View.VISIBLE
                    btnApprove.setBackgroundResource(R.drawable.bg_leave_waiting)
                    btnApprove.text = "Waiting"
                }

                Constant.approved -> {
                    btnCancel.visibility = View.GONE
                    btnApprove.visibility = View.VISIBLE
                    btnApprove.setBackgroundResource(R.drawable.bg_leave_approved)
                    btnApprove.text = "Approved"
                }

                Constant.rejected-> {
                    btnCancel.visibility = View.GONE
                    btnApprove.visibility = View.VISIBLE
                    btnApprove.setBackgroundResource(R.drawable.bg_leave_rejected)
                    btnApprove.text = "Rejected"
                }
            }


        }
        }


    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}