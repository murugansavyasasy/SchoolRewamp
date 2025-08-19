package com.vs.schoolmessenger.Parent.Timetable

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R

class TimeTableDayAdapter(
    private var itemList: List<TimeTableDayData>?,
    private var listener: TimeTableDayListener,
    private var context: Context,
    private var isLoading: Boolean

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var selectedPosition = 0

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    fun setSelectedPosition(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousPosition)
        notifyItemChanged(selectedPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.shimmer_view_small_list, parent, false)
            DataViewHolder.ShimmerViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_day, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList!![position], position, listener, this)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val day_values: TextView = itemView.findViewById(R.id.btnDay)
        private val btnDay: TextView = itemView.findViewById(R.id.btnDay)
        private val rytCard: RelativeLayout = itemView.findViewById(R.id.rytCard)

        fun bind(
            data: TimeTableDayData,
            position: Int,
            listener: TimeTableDayListener,
            adapter: TimeTableDayAdapter
        ) {
            day_values.text = data.day_values

            val isSelected = adapter.selectedPosition == position
            rytCard.background = ContextCompat.getDrawable(
                context,
                if (isSelected) R.drawable.day_selected else R.drawable.day_unselected
            )

            btnDay.setOnClickListener {
                listener.onItemClick(data)
                adapter.setSelectedPosition(position)
            }
        }

        class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val shimmerLayout: ShimmerFrameLayout =
                itemView.findViewById(R.id.shimmer_view_container)

            init {
                shimmerLayout.startShimmer()
            }
        }
    }
}
