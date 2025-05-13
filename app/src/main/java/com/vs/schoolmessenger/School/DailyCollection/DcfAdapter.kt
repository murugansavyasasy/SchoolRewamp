package com.vs.schoolmessenger.School.DailyCollection

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R

class DcfAdapter(
    private var itemList: List<DisplayItem>,
    private val context: Context,
    private val isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_HEADER = 1
    private val TYPE_FEE = 2

    override fun getItemViewType(position: Int): Int {
        return when {
            isLoading -> TYPE_SHIMMER
            itemList[position] is DisplayItem.Header -> TYPE_HEADER
            itemList[position] is DisplayItem.Fee -> TYPE_FEE
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.dcf_recycle_1, parent, false)
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(view)
            TYPE_FEE -> FeeViewHolder(view)
            else -> ShimmerViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder && itemList[position] is DisplayItem.Header) {
            holder.bind(itemList[position] as DisplayItem.Header)
        } else if (holder is FeeViewHolder && itemList[position] is DisplayItem.Fee) {
            holder.bind(itemList[position] as DisplayItem.Fee)
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int = if (isLoading) 20 else itemList.size

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val totalLabel: TextView = itemView.findViewById(R.id.total_label)
        private val totalValue: TextView = itemView.findViewById(R.id.total_value)

        fun bind(item: DisplayItem.Header) {
            totalLabel.text = item.category
            totalValue.text = item.total
        }
    }

    inner class FeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val totalLabel: TextView = itemView.findViewById(R.id.total_label)
        private val totalValue: TextView = itemView.findViewById(R.id.total_value)

        fun bind(item: DisplayItem.Fee) {
            totalLabel.text = item.typeName
            totalValue.text = item.amount
        }
    }

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout = itemView.findViewById(R.id.shimmer_view_container)
        fun startShimmer() {
            shimmerLayout.startShimmer()
        }
    }
}


//BS
//package com.vs.schoolmessenger.School.DailyCollection
//
//
//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.facebook.shimmer.ShimmerFrameLayout
//import com.vs.schoolmessenger.R
//import com.vs.schoolmessenger.School.Homework.HomeWorkReportAdapter.ShimmerViewHolder
//
//
//class DcfAdapter(
//    private var itemList: List<FeeData>?,
//    private val listener: DailyCollection,
//    private val context: Context,
//    private val isLoading: Boolean
//) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//
//    private val TYPE_SHIMMER = 0
//    private val TYPE_DATA = 1
//
//    override fun getItemViewType(position: Int): Int {
//        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return if (viewType == TYPE_SHIMMER) {
//            val view = LayoutInflater.from(parent.context)
//                .inflate(R.layout.shimmer_view_small_list, parent, false)
//            ShimmerViewHolder(view)
//        } else {
//            val view = LayoutInflater.from(parent.context)
//                .inflate(R.layout.dcf_recycle_1, parent, false)
//            DataViewHolder(view)
//        }
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        if (holder is DataViewHolder) {
//            itemList?.get(position)?.let { holder.bind(it) }
//        } else if (holder is ShimmerViewHolder) {
//            holder.startShimmer()
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return if (isLoading) 20 else itemList?.size ?: 0
//    }
//
//    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val totalLabel: TextView = itemView.findViewById(R.id.total_label)
//        private val totalValue: TextView = itemView.findViewById(R.id.total_value)
//
//        fun bind(feeData: FeeData) {
//            totalLabel.text = feeData.type_name.ifEmpty { "Unnamed Type" }
//            totalValue.text = feeData.amount
//        }
//    }
//
//    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val shimmerLayout: ShimmerFrameLayout =
//            itemView.findViewById(R.id.shimmer_view_container)
//
//        fun startShimmer() {
//            shimmerLayout.startShimmer()
//        }
//    }
//}



//Existing Code--Default
//package com.vs.schoolmessenger.School.DailyCollection
//
//
//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.facebook.shimmer.ShimmerFrameLayout
//import com.vs.schoolmessenger.R
//
//
//class DcfAdapter (
//    private var itemList: List<DailyCollectionItem>?,
//    private val listener: DcfClickListener,
//    private val context: Context,
//    private val isLoading: Boolean
//
//) : RecyclerView.Adapter<RecyclerView.ViewHolder> () {
//
//    private val TYPE_SHIMMER = 0
//    private val TYPE_DATA = 1
//
//    override fun getItemViewType(position: Int): Int {
//        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return if (viewType == TYPE_SHIMMER) {
//            val view = LayoutInflater.from(parent.context)
//                .inflate(R.layout.shimmer_view_small_list, parent, false)
//            DataViewHolder.ShimmerViewHolder(view)
//        } else {
//            val view = LayoutInflater.from(parent.context)
//                .inflate(R.layout.dcf_recycle_1, parent, false)
//            DataViewHolder(view, context) // Pass context to DataViewHolder
//        }
//    }
//
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//
//    }
//
//
//
//    override fun getItemCount(): Int {
//        return if (isLoading) 20 // Show shimmer items while loading
//        else itemList?.size ?: 0
//    }
//
//
//    class DataViewHolder(itemView: View, private val context: Context) :
//        RecyclerView.ViewHolder(itemView) {
//        private val total_label: TextView = itemView.findViewById(R.id.total_label)
//        private val total_value: TextView = itemView.findViewById(R.id.total_value)
//
//        fun bind(
//            position: Int,
//            listener: DcfClickListener,
//            adapter: DcfAdapter
//        ) {
//
//
//        }
//
//        class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//            private val shimmerLayout: ShimmerFrameLayout =
//                itemView.findViewById(R.id.shimmer_view_container)
//            init {
//                shimmerLayout.startShimmer() // Start shimmer effect
//            }
//        }
//    }
//}
