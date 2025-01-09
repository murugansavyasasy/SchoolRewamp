package com.vs.schoolmessenger.Parent.EventsHolidays

import android.annotation.SuppressLint
import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R

class HolidayAdapter(
    private var itemList: List<HolidayData>?,
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
                    .inflate(R.layout.holiday_item, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList!![position], position, this)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {


        private val lblMonth: TextView = itemView.findViewById(R.id.lblMonth)
        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        private val lblNameOfTheDate: TextView = itemView.findViewById(R.id.lblNameOfTheDate)
        private val lblNameOfTheHoliDay: TextView = itemView.findViewById(R.id.lblNameOfTheHoliDay)


        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            data: HolidayData,
            position: Int,
            adapter: HolidayAdapter
        ) {
            lblNameOfTheHoliDay.text = data.reason
            lblDate.text = "01"
            lblMonth.text = "APR"
            lblNameOfTheDate.text = "Sunday"

        }
    }
}


class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val shimmerLayout: ShimmerFrameLayout =
        itemView.findViewById(R.id.shimmer_view_container)

    init {
        shimmerLayout.startShimmer() // Start shimmer effect
    }
}
