package com.vs.schoolmessenger.Parent.RequestLeave

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Attachment.DataClass.AttachmentDataReport
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class MonthWiseLeaveHistoryAdapter(
    private var itemList: List<MonthWiseLeaveData>?,
    private val context: Context,
    private val leaveRequestClickListener: LeaveRequestClickListener,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var fullList: List<MonthWiseLeaveData> = itemList ?: emptyList()
    private var filteredList: List<MonthWiseLeaveData> = fullList

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.monthwise_leave_history_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.monthwise_leave_history_item, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else filteredList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(filteredList[position], leaveRequestClickListener)
        }
    }

    fun updateData(newList: List<MonthWiseLeaveData>) {
        fullList = newList
        filteredList = newList
        notifyDataSetChanged()
    }



    fun getCurrentList(): List<MonthWiseLeaveData> {
        return itemList!!
    }


    fun removeItemById(id: String) {
        val updatedList = fullList.mapNotNull { monthData ->
            val updatedDetails = monthData.details.filterNot { it.id == id }
            if (updatedDetails.isNotEmpty()) {
                MonthWiseLeaveData(month = monthData.month, details = updatedDetails)
            } else null
        }
        updateData(updatedList)
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val lblMonthName: TextView = itemView.findViewById(R.id.lblMonthName)
        private val rvMonthWiseHistory: RecyclerView =
            itemView.findViewById(R.id.rvMonthWiseHistory)

        fun bind(
            data: MonthWiseLeaveData,
            leaveRequestClickListener: LeaveRequestClickListener
        ) {
            lblMonthName.text = data.month

            if (data.details.isEmpty()) {
                rvMonthWiseHistory.visibility = View.GONE
            } else {
                rvMonthWiseHistory.visibility = View.VISIBLE
                rvMonthWiseHistory.layoutManager = LinearLayoutManager(context)
                rvMonthWiseHistory.isNestedScrollingEnabled = false
                rvMonthWiseHistory.adapter = LeaveRequestAdapter(
                    data.details,
                    leaveRequestClickListener,
                    context,
                    false
                )
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
