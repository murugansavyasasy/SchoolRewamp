//Last Working Code
package com.vs.schoolmessenger.School.LeaveRequests

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.RequestLeave.MonthWiseLeaveData
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.LeaveRequests.Listener.SchoolLRClickListener
import com.vs.schoolmessenger.Utils.ShimmerUtil

class MonthwiseLeaveAdapter(
    private var itemList: List<MonthWiseLeaveData>?,
    private val context: Context,
    private val leaveRequestClickListener: SchoolLRClickListener,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), android.widget.Filterable {

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


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""

                val result = if (query.isEmpty()) {
                    fullList
                } else {
                    fullList.mapNotNull { monthData ->
                        val filteredDetails = monthData.details.filter {
                            it.student_name.lowercase().contains(query) ||
                                    it.section_name.lowercase().contains(query) ||
                                    it.reason.lowercase()
                                        .contains(query) || it.no_of_days.lowercase()
                                .contains(query)
                                    || it.leave_type.lowercase()
                                .contains(query) || it.class_name.lowercase().contains(query)

                        }
                        if (filteredDetails.isNotEmpty()) {
                            MonthWiseLeaveData(month = monthData.month, details = filteredDetails)
                        } else {
                            null
                        }
                    }
                }

                return FilterResults().apply { values = result }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<MonthWiseLeaveData> ?: emptyList()
                leaveRequestClickListener.onSearchResultEmpty(filteredList.isEmpty())
                notifyDataSetChanged()
            }
        }
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val lblMonthName: TextView = itemView.findViewById(R.id.lblMonthName)
        private val rvMonthWiseHistory: RecyclerView =
            itemView.findViewById(R.id.rvMonthWiseHistory)


        fun bind(
            data: MonthWiseLeaveData,
            leaveRequestClickListener: SchoolLRClickListener
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
