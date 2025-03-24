package com.vs.schoolmessenger.Parent.LSRW

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R

class LSRWSummaryAdapter (
    private var itemList: List<LSRWSummaryData>?,
    private var listener: LSRWSummaryClickListener,
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
                .inflate(R.layout.lsrw_summarylist, parent, false)
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
        private val attachment_value: TextView = itemView.findViewById(R.id.attachment_value)
        private val reason_value: TextView = itemView.findViewById(R.id.reason_value)


        fun bind(
            data: LSRWSummaryData,
            position: Int,
            listener: LSRWSummaryClickListener,
            adapter: LSRWSummaryAdapter
        ) {
            attachment_value.text = data.attachment_value
            reason_value.text = data.reason_value
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
