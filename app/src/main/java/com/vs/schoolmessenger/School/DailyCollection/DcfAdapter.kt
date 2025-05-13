package com.vs.schoolmessenger.School.DailyCollection


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R


class DcfAdapter (
    private var itemList: List<DailyCollectionItem>?,
    private val listener: DcfClickListener,
    private val context: Context,
    private val isLoading: Boolean

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
            position: Int,
            listener: DcfClickListener,
            adapter: DcfAdapter
        ) {


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
