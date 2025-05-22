package com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Adapter

import android.annotation.SuppressLint
import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Model.Holiday
import com.vs.schoolmessenger.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HolidayAdapter(
    private var itemList: List<Holiday>?,
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
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.shimmer_view_small_list, parent, false)
            ShimmerViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.holiday_item, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList!![position], position, this)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val lblMonth: TextView = itemView.findViewById(R.id.lblMonth)
        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        private val lblNameOfTheDate: TextView = itemView.findViewById(R.id.lblNameOfTheDate)
        private val lblNameOfTheHoliDay: TextView = itemView.findViewById(R.id.lblNameOfTheHoliDay)

        @SuppressLint("SimpleDateFormat")
        fun bind(
            data: Holiday,
            position: Int,
            adapter: HolidayAdapter
        ) {
            lblNameOfTheHoliDay.text = data.name

            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date: Date? = inputFormat.parse(data.date)

                if (date != null) {
                    val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
                    val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
                    val dayNameFormat = SimpleDateFormat("EEEE", Locale.getDefault())

                    lblDate.text = dayFormat.format(date)
                    lblMonth.text = monthFormat.format(date).uppercase()
                    lblNameOfTheDate.text = dayNameFormat.format(date)
                } else {
                    lblDate.text = "--"
                    lblMonth.text = "--"
                    lblNameOfTheDate.text = "--"
                }
            } catch (e: Exception) {
                lblDate.text = "--"
                lblMonth.text = "--"
                lblNameOfTheDate.text = "--"
            }
        }
    }
}

class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val shimmerLayout: ShimmerFrameLayout =
        itemView.findViewById(R.id.shimmer_view_container)

    init {
        shimmerLayout.startShimmer()
    }
}
