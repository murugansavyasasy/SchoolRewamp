package com.vs.schoolmessenger.School.Event.Adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Event.Listener.SchoolEventClickListener
import com.vs.schoolmessenger.School.Event.Model.SchoolEventItem
import com.vs.schoolmessenger.Utils.ShimmerUtil

class SchoolEventAdapter(
    private val originalList: MutableList<SchoolEventItem>,
    private val listener: SchoolEventClickListener,
    private val context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private var filteredList: MutableList<SchoolEventItem> = originalList.toMutableList()

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.event_ongoing_recyclerview)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.event_ongoing_recyclerview, parent, false)
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(filteredList[position], listener)
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
                val query = constraint?.toString()?.trim()?.lowercase() ?: ""
                val resultsList = if (query.isEmpty()) {
                    originalList
                } else {
                    originalList.filter {
                        it.title?.lowercase()?.contains(query) == true ||
                                it.description?.lowercase()?.contains(query) == true
                    }
                }
                return FilterResults().apply { values = resultsList }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList.clear()
                filteredList.addAll(results?.values as List<SchoolEventItem>)
                notifyDataSetChanged()

                (context as? Activity)?.runOnUiThread {
                    val noResultsText = context.findViewById<TextView>(R.id.noDataText)
                    val noResultsImage = context.findViewById<ImageView>(R.id.noDataImage)

                    if (filteredList.isEmpty()) {
                        noResultsText?.visibility = View.VISIBLE
                        noResultsImage?.visibility = View.VISIBLE
                    } else {
                        noResultsText?.visibility = View.GONE
                        noResultsImage?.visibility = View.GONE
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<SchoolEventItem>) {
        originalList.clear()
        originalList.addAll(newList)
        filteredList.clear()
        filteredList.addAll(newList)
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
