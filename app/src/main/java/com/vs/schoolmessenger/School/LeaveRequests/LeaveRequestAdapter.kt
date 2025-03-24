package com.vs.schoolmessenger.School.LeaveRequests

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.text.font.FontFamily
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R

class LeaveRequestAdapter(
    private var itemList: List<LeaveRequestData>?,
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
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.shimmer_view_small_list, parent, false)
            ShimmerViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.leave_request_list, parent, false)
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
        private val lblName: TextView = itemView.findViewById(R.id.lblName)
        private val lblSection: TextView = itemView.findViewById(R.id.lblSection)
        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        private val lblFromData: TextView = itemView.findViewById(R.id.lblFromData)
        private val lblToDate: TextView = itemView.findViewById(R.id.lblToDate)
        private val lblReason: TextView = itemView.findViewById(R.id.lblReason)

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: LeaveRequestData, position: Int) {

            lblName.text=data.isName
            lblSection.text=data.isSection
            lblDate.text=data.isDate + " - " +data.isTime
            lblFromData.text=data.isLeaveFromDate
            lblToDate.text=data.isLeaveToDate
            lblReason.text=data.isReason
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmer_view_container)

        init {
            shimmerLayout.startShimmer() // Start shimmer effect
        }
    }
}