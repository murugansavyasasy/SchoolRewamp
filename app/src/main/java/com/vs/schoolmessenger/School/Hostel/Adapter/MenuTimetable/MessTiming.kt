package com.vs.schoolmessenger.School.Hostel.Adapter.MenuTimetable


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Hostel.Model.MessTimeTable.MessTiming.MessTimingData
import com.vs.schoolmessenger.Utils.ShimmerUtil

class MessTiming(
    private var itemList: List<MessTimingData>?,
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
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.mess_timing_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.mess_timing_item, parent, false)
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            itemList?.get(position)?.let { holder.bind(it, position) }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }


    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList?.size ?: 0
    }

    fun updateData(newList: List<MessTimingData>) {
        itemList = newList
        notifyDataSetChanged()
    }



    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblMealName: TextView = itemView.findViewById(R.id.lblMealName)
        private val lblTime: TextView = itemView.findViewById(R.id.lblTime)


        fun bind(data: MessTimingData, position: Int) {
            lblMealName.text = data.mealName
            lblTime.text = data.time

        }

    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)

        }
    }

}