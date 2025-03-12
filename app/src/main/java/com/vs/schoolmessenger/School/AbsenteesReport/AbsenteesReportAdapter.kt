package com.vs.schoolmessenger.School.AbsenteesReport

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Communication.TextHistoryAdapter
import com.vs.schoolmessenger.School.Communication.TextHistoryAdapter.DataViewHolder
import com.vs.schoolmessenger.School.Communication.TextHistoryClickListener
import com.vs.schoolmessenger.School.Communication.TextHistoryData


class AbsenteesReportAdapter (

    private var itemList: List<AbsenteesDateData>?,
    private var listener: AbsenteesClickListener,
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
                .inflate(R.layout.absentees_date_list, parent, false)
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

    fun setSelectedPosition(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousPosition)
        notifyItemChanged(selectedPosition)
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val description_view: TextView = itemView.findViewById(R.id.description_view)
       private val linearLayout1: LinearLayout = itemView.findViewById(R.id.linear_layout1)

        fun bind(
            data: AbsenteesDateData,
            position: Int,
            listener: AbsenteesClickListener,
            adapter: AbsenteesReportAdapter
        ) {
            titleTextView.text = data.Month
            descriptionTextView.text = data.Date
            description_view.text = data.Day

            if (adapter.selectedPosition == position) {
                linearLayout1.setBackgroundColor(ContextCompat.getColor(context,R.color.custom_blue))
                descriptionTextView.setTextColor(ContextCompat.getColor(context,R.color.black))
                description_view.setTextColor(ContextCompat.getColor(context,R.color.black))
            } else {
                linearLayout1.setBackgroundColor(ContextCompat.getColor(context,R.color.white))
                descriptionTextView.setTextColor(ContextCompat.getColor(context,R.color.grey))
                description_view.setTextColor(ContextCompat.getColor(context,R.color.grey))

            }

            itemView.setOnClickListener {
                adapter.setSelectedPosition(position)  // Update selected position
            }
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
