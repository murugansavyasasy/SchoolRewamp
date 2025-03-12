package com.vs.schoolmessenger.School.DailyCollection

import com.vs.schoolmessenger.School.AbsenteesReport.AbsenteesClickListener
import com.vs.schoolmessenger.School.AbsenteesReport.AbsenteesDateData


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


class DcfAdapter1 (

    private var itemList: List<DcfData1>?,
    private var listener: Dcf1ClickListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder> () {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

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
                .inflate(R.layout.dcf_recycle_1, parent, false)
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
        private val total_label: TextView = itemView.findViewById(R.id.total_label)
        private val total_value: TextView = itemView.findViewById(R.id.total_value)

        fun bind(
            data: DcfData1,
            position: Int,
            listener: Dcf1ClickListener,
            adapter: DcfAdapter1
        ) {
            total_label.text = data.label
            total_value.text = data.labelvalues

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
