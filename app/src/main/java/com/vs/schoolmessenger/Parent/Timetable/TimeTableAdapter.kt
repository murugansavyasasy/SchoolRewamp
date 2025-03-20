package com.vs.schoolmessenger.Parent.Timetable

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.Parent.LSRW.LSRWAdapter
import com.vs.schoolmessenger.Parent.LSRW.LSRWClickListener
import com.vs.schoolmessenger.Parent.LSRW.LSRWData
import com.vs.schoolmessenger.R

class TimeTableAdapter (
    private var itemList: List<TimeTableData>?,
    private var listener: TimeTableListener,
    private var context: Context,
    private var isLoading: Boolean

) : RecyclerView.Adapter<RecyclerView.ViewHolder> () {
    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var selectedPosition = RecyclerView.NO_POSITION


    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.shimmer_view_small_list, parent, false)
            DataViewHolder.ShimmerViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_timetable, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
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
        private val total_time: TextView = itemView.findViewById(R.id.total_time)
        private val duration_value: TextView = itemView.findViewById(R.id.duration_value)


        fun bind(
            data: TimeTableData,
            position: Int,
            listener: TimeTableListener,
            adapter: TimeTableAdapter
        ) {
            time.text = data.time
            subject_value.text = data.subject
            name_value.text = data.teacher
            total_time.text = data.schedule
            duration_value.text = data.duration
        }

        class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val shimmerLayout: ShimmerFrameLayout =
                itemView.findViewById(R.id.shimmer_view_container)
            init {
                shimmerLayout.startShimmer() // Start shimmer effect
            }
        }
    }
}
