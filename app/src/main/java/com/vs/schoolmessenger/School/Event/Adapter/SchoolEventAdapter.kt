package com.vs.schoolmessenger.School.Event.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Adapter.EventAdapter.DataViewHolder
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Event.Listener.SchoolEventClickListener
import com.vs.schoolmessenger.School.Event.Model.SchoolEventItem
import com.vs.schoolmessenger.Utils.ShimmerUtil

class SchoolEventAdapter(
    private var itemList: List<SchoolEventItem>?,
    private val listener: SchoolEventClickListener,
    private val context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var fullList: List<SchoolEventItem> = itemList ?: listOf()
    private var filteredList: List<SchoolEventItem> = itemList ?: listOf()

    init {
        fullList = itemList ?: listOf()
        filteredList = fullList
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.event_ongoing_recyclerview)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.event_ongoing_recyclerview, parent, false)
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            filteredList[position].let {
                holder.bind(it, listener)
            }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 3 else filteredList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""
                val result = if (query.isEmpty()) {
                    fullList
                } else {
                    val filtered = fullList.filter {
                        (it.title?.lowercase()?.contains(query) == true) ||
                                (it.description?.lowercase()?.contains(query) == true) ||
                                (it.venue?.lowercase()?.contains(query) == true)
                    }
                    filtered
                }
                return FilterResults().apply { values = result }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<SchoolEventItem> ?: listOf()
                listener.onSearchResultEmpty("ONGOING", filteredList.isEmpty())
                notifyDataSetChanged()
            }
        }
    }


    fun updateList(newList: List<SchoolEventItem>?) {
        this.itemList = newList ?: listOf()
        fullList = this.itemList!!
        filteredList = fullList
        notifyDataSetChanged()
    }


    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val event_header: TextView = itemView.findViewById(R.id.event_header)
        private val event_time: TextView = itemView.findViewById(R.id.event_time)
        private val event_location: TextView = itemView.findViewById(R.id.event_location)
        private val status_event: TextView = itemView.findViewById(R.id.status_event)
        private val eventdesc: TextView = itemView.findViewById(R.id.eventdesc)

        fun bind(data: SchoolEventItem, listener: SchoolEventClickListener) {
            event_header.text = data.title
            event_time.text = "Event started at ${data.time} - ${data.date}"
            event_location.text = data.venue
            status_event.text = "Today's Event"
            eventdesc.text = data.description
        }
    }
}

class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun startShimmer() {
        ShimmerUtil.startShimmer(itemView)
    }
}
