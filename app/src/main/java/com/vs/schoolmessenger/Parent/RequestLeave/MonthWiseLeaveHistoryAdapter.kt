package com.vs.schoolmessenger.Parent.RequestLeave


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil

class MonthWiseLeaveHistoryAdapter(
    private var itemList: List<MonthWiseLeaveData>?,
    private var context: Context,
    private val leaveRequestClickListener: LeaveRequestClickListener,
    private var isLoading: Boolean,

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var fullList: List<MonthWiseLeaveData> = itemList ?: listOf()
    private var filteredList: List<MonthWiseLeaveData> = fullList

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {

            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.monthwise_leave_history_item)
            ShimmerViewHolder(
                shimmerView
            )
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.monthwise_leave_history_item, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else filteredList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(filteredList[position], position, this, leaveRequestClickListener)
        }
    }


    fun updateData(newList: List<MonthWiseLeaveData>) {
        fullList = newList
        filteredList = newList
        notifyDataSetChanged()
    }


fun filterByStatus(status: String) {
    filteredList = if (status.equals("All", ignoreCase = true)) {
        fullList
    } else {
        fullList.mapNotNull { monthData ->
            val filteredDetails = monthData.details.filter {
                it.status.equals(status, ignoreCase = true)
            }
            if (filteredDetails.isNotEmpty()) {
                MonthWiseLeaveData(month = monthData.month, details = filteredDetails)
            } else null
        }
    }
    notifyDataSetChanged()
}



    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private var isTextExpanded = false

        private val lblMonthName: TextView = itemView.findViewById(R.id.lblMonthName)
        private val rvMonthWiseHistory: RecyclerView= itemView.findViewById(R.id.rvMonthWiseHistory)


        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            data: MonthWiseLeaveData,
            position: Int,
            adapter: MonthWiseLeaveHistoryAdapter,
            leaveRequestClickListener: LeaveRequestClickListener
        ) {
            lblMonthName.text = data.month

            if (data.details.isEmpty()) {
                rvMonthWiseHistory.visibility = View.GONE
            } else {
                rvMonthWiseHistory.visibility = View.VISIBLE
                rvMonthWiseHistory.layoutManager = LinearLayoutManager(
                    itemView.context,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                rvMonthWiseHistory.isNestedScrollingEnabled = false
                rvMonthWiseHistory.adapter = LeaveRequestAdapter(
                    data.details,
                    leaveRequestClickListener,
                    itemView.context,
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