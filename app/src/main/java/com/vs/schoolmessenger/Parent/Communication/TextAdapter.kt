package com.vs.schoolmessenger.Parent.Communication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R

class TextAdapter(
    private var itemList: ArrayList<TextData>?,
    private var listener: TextClickListener,
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
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.shimmer_view_small_list, parent, false)
            DataViewHolder.ShimmerViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.history_from_text_message, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }


    fun updateData() {
        itemList!!.clear()
        notifyDataSetChanged() // Notify RecyclerView of data changes
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
        private val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        private val lblContent: TextView = itemView.findViewById(R.id.lblContent)
        private val lblSeeMore: TextView = itemView.findViewById(R.id.lblSeeMore)
        private val rlaSelectText: RelativeLayout = itemView.findViewById(R.id.rlaSelectText)
        private var isExpanded = false


        fun bind(
            data: TextData,
            position: Int,
            listener: TextClickListener,
            adapter: TextAdapter
        ) {
            lblTitle.text = data.title
            lblDate.text = data.date
            lblContent.text = data.content
            rlaSelectText.visibility = View.GONE
            lblSeeMore.setOnClickListener {
                isExpanded = !isExpanded
                updateTextView()
            }

        }

        private fun updateTextView() {
            if (isExpanded) {
                // Expand the TextView to show all lines
                lblContent.maxLines = Int.MAX_VALUE
                lblSeeMore.text = "See Less" // Change button text to "See Less"
            } else {
                // Collapse the TextView to show a maximum of 3 lines
                lblContent.maxLines = 3
                lblSeeMore.text = "See More" // Change button text back to "See More"
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
