package com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Model.EventClickListener
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.RewampModelEvent.EventItem
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class EventUpcomingAdapter (
    private var itemList: List<EventItem>?,
    private var listener: EventClickListener,
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
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.event_upcoming_recyclerview)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.event_upcoming_recyclerview, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            itemList?.get(position)?.let {
                holder.bind(it, position, listener, this)
            }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }


    override fun getItemCount(): Int {
        return if (isLoading) 10 else itemList?.size ?: 0
    }





    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val event_header: TextView = itemView.findViewById(R.id.event_header)
        private val event_time: TextView = itemView.findViewById(R.id.event_time)
        private val event_location: TextView = itemView.findViewById(R.id.event_location)

        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            data: EventItem,
            position: Int,
            listener: EventClickListener,
            adapter: EventUpcomingAdapter
        ) {
            event_header.text = data.title
            event_time.text = "Event will start at"+" "+data.time + " - " + Constant.convertDateTimeFormat(data.date)
            event_location.text = data.venue
        }
    }
}
