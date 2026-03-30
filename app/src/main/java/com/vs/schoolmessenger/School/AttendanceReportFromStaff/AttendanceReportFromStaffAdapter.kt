package com.vs.schoolmessenger.School.AttendanceReportFromStaff

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Communication.Adapter.TextHistoryAdapter
import com.vs.schoolmessenger.Utils.ShimmerUtil
import java.text.SimpleDateFormat
import java.util.Locale

class AttendanceReportFromStaffAdapter(
    private var itemList: List<Pair<String, DateAttendanceDataClass>>?,
    private var context: Context,
    private var isLoading: Boolean,
    private val listener: OnAttendanceHistoryClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.item_staff_attendate_date_report)
            TextHistoryAdapter.DataViewHolder.ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_staff_attendate_date_report, parent, false)
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList!![position])
        } else if (holder is TextHistoryAdapter.DataViewHolder.ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 10 else itemList?.size ?: 0
    }

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(data: Pair<String, DateAttendanceDataClass>) {

            val date = data.first
            val value = data.second

            val tvDay = itemView.findViewById<TextView>(R.id.tvDay)
            val tvWeek = itemView.findViewById<TextView>(R.id.tvWeek)
            val tvFullDate = itemView.findViewById<TextView>(R.id.tvFullDate)
            val tvSummary = itemView.findViewById<TextView>(R.id.tvSummary)
            val recyclerChild = itemView.findViewById<RecyclerView>(R.id.recyclerChild)

            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
            val weekFormat = SimpleDateFormat("EEE", Locale.getDefault())

            val parsedDate = inputFormat.parse(date)

            if (parsedDate != null) {
                tvDay.text = dayFormat.format(parsedDate)
                tvWeek.text = weekFormat.format(parsedDate)
                tvFullDate.text = outputFormat.format(parsedDate)
            }

            tvSummary.text =
                "Present: ${value.stat.present} • Absent: ${value.stat.absent} • Not Marked: ${value.stat.not_marked}"

            recyclerChild.layoutManager = LinearLayoutManager(context)

            recyclerChild.adapter =
                AttendanceChildAdapter(value.attd_details, listener)
        }
    }
}