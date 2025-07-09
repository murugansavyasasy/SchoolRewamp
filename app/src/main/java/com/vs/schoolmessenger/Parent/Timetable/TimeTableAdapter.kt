package com.vs.schoolmessenger.Parent.Timetable

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil

class TimeTableAdapter(
    private var itemList: List<TimeTableListData>?,
    private var listener: TimeTableListener,
    private var context: Context,
    private var isLoading: Boolean

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var selectedPosition = RecyclerView.NO_POSITION

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.item_timetable)
            DataViewHolder.ShimmerViewHolder(shimmerView)

        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_timetable, parent, false)
            DataViewHolder(
                view,
                context
            ) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            holder.bind(itemList!![position], position, listener, this) // Pass adapter reference
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val time: TextView = itemView.findViewById(R.id.time_value)
        private val subject_value: TextView = itemView.findViewById(R.id.subject_value)
        private val name_value: TextView = itemView.findViewById(R.id.name_value)
        private val lblFromToTime: TextView = itemView.findViewById(R.id.lblFromToTime)
        private val duration_value: TextView = itemView.findViewById(R.id.duration_value)


        fun bind(
            data: TimeTableListData,
            position: Int,
            listener: TimeTableListener,
            adapter: TimeTableAdapter
        ) {
            time.text = data.name

            if (data.subject_name.isNotEmpty()) {
                subject_value.visibility = View.VISIBLE
                subject_value.text = data.subject_name
            } else {
                subject_value.visibility = View.GONE
            }

            if (data.staff_name.isNotEmpty()) {
                name_value.visibility = View.VISIBLE
                name_value.text = data.staff_name
            } else {
                name_value.visibility = View.GONE
            }
            lblFromToTime.text = data.start_time + " - " + data.end_time
            duration_value.text = data.duration + " Minutes"
        }

        class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun startShimmer() {
                ShimmerUtil.startShimmer(itemView)
            }
        }
    }
}
