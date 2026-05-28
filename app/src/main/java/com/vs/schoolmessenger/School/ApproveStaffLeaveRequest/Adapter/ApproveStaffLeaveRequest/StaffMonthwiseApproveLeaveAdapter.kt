package com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.Adapter.ApproveStaffLeaveRequest

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.RequestLeave.MonthWiseLeaveData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.Model.StaffLeaveRequestHistory.StaffMonthWiseLeaveData
import com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.listner.ApproveStaffLeaveRequestClickListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class StaffMonthwiseApproveLeaveAdapter(
    private var itemList: List<StaffMonthWiseLeaveData>?,
    private val context: Context,
    private val approveStaffLeaveRequest: ApproveStaffLeaveRequestClickListener,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var currentStatusFilter: String = Constant.All_


    var fullList: List<StaffMonthWiseLeaveData> = itemList ?: emptyList()
    var filteredList: List<StaffMonthWiseLeaveData> = fullList

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

    fun filterByStatus(status: String) {
        currentStatusFilter = status
        filter.filter("") // trigger filter with empty query to apply status
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(filteredList[position], approveStaffLeaveRequest)
        }
    }

    fun updateData(newList: List<StaffMonthWiseLeaveData>) {
        fullList = newList
        filteredList = newList
        notifyDataSetChanged()
    }

// Add a variable to track the selected status

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""

                val result = fullList.mapNotNull { monthData ->
                    // First, filter details by status
                    val statusFiltered = monthData.details.filter { leave ->
                        currentStatusFilter == Constant.All_ || leave.status.equals(
                            currentStatusFilter,
                            ignoreCase = true
                        )
                    }

                    // Then, apply text query on the filtered list
                    val finalFiltered = if (query.isEmpty()) {
                        statusFiltered
                    } else {
                        statusFiltered.filter { leave ->
                            (leave.staff_name?:"").lowercase().contains(query) ||
                                    (leave.reason?:"").lowercase().contains(query) ||
                                    (leave.no_of_days.toString()?:"").lowercase().contains(query) ||
                                    (leave.leave_type?:"").lowercase().contains(query)
                        }
                    }

                    if (finalFiltered.isNotEmpty()) {
                        StaffMonthWiseLeaveData(month = monthData.month, details = finalFiltered)
                    } else null
                }

                return FilterResults().apply { values = result }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<StaffMonthWiseLeaveData> ?: emptyList()
                approveStaffLeaveRequest.onSearchResultEmpty(filteredList.isEmpty())
                notifyDataSetChanged()
            }
        }
    }


    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val lblMonthName: TextView = itemView.findViewById(R.id.lblMonthName)
        val rvMonthWiseHistory: RecyclerView =
            itemView.findViewById(R.id.rvMonthWiseHistory)


        fun bind(
            data: StaffMonthWiseLeaveData,
            approveStaffLeaveRequest: ApproveStaffLeaveRequestClickListener
        ) {
            lblMonthName.text = data.month

            if (data.details.isEmpty()) {
                rvMonthWiseHistory.visibility = View.GONE
            } else {
                rvMonthWiseHistory.visibility = View.VISIBLE
                rvMonthWiseHistory.layoutManager = LinearLayoutManager(context)
                rvMonthWiseHistory.isNestedScrollingEnabled = false
                rvMonthWiseHistory.adapter = StaffApproveLeaveRequestAdapter(
                    data.details,
                    approveStaffLeaveRequest,
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