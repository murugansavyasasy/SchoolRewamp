package com.vs.schoolmessenger.Parent.RequestLeave

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R

class LeaveRequestAdapter(
    private var itemList: List<LeaveRequestHistoryData>?,
    private var listener: LeaveRequestClickListener,
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
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.shimmer_view_small_list, parent, false)
            ShimmerViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.leave_request_history_item, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            holder.bind(itemList!![position], position, listener, this)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private var isTextExpanded = false


        private val lblFrom: TextView = itemView.findViewById(R.id.lblFrom)
        private val lblTo: TextView = itemView.findViewById(R.id.lblTo)
        private val lblStatus: TextView = itemView.findViewById(R.id.lblStatus)
        private val lblReason: TextView = itemView.findViewById(R.id.lblReason)
        private val tvSeeMoreImage: TextView = itemView.findViewById(R.id.tvSeeMoreImage)

        private val rlaStatus: RelativeLayout = itemView.findViewById(R.id.rlaStatus)
        private val imgStatus: ImageView = itemView.findViewById(R.id.imgStatus)

        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            data: LeaveRequestHistoryData,
            position: Int,
            listener: LeaveRequestClickListener,
            adapter: LeaveRequestAdapter
        ) {

            lblFrom.text = data.isFrom
            lblTo.text = data.isTo
            lblStatus.text = data.isStatus
            lblReason.text = data.isReason

            isSeeMoreVisibility(lblReason, tvSeeMoreImage)

            when (data.isStatus) {

                "Pending" -> {
                    rlaStatus.setBackgroundResource(R.drawable.bg_light_orange)
                    imgStatus.setImageResource(R.drawable.approval_icon)
                }

                "Approval" -> {
                    rlaStatus.setBackgroundResource(R.drawable.bg_green_radoius_10dp)
                    imgStatus.setImageResource(R.drawable.approval_icon)
                }

                "Rejected" -> {
                    rlaStatus.setBackgroundResource(R.drawable.bg_red_radious__all_side_same)
                    imgStatus.setImageResource(R.drawable.close_icon_red)
                }
            }

            tvSeeMoreImage.setOnClickListener {
                isSeeMoreExpanded(tvSeeMoreImage, lblReason)
            }
        }

        private fun isSeeMoreExpanded(tvSeeMore: TextView, lblContent: TextView) {

            if (isTextExpanded) {
                isTextExpanded = false
                lblContent.maxLines = 3
                lblContent.ellipsize = TextUtils.TruncateAt.END
                tvSeeMore.text = context.getString(R.string.SeeMore)
            } else {
                isTextExpanded = true
                lblContent.maxLines = Integer.MAX_VALUE
                lblContent.ellipsize = null
                tvSeeMore.text = context.getString(R.string.SeeLess)
            }
        }

        private fun isSeeMoreVisibility(lblContent: TextView, tvSeeMore: TextView) {
            lblContent.post {
                if (lblContent.lineCount > 3) {
                    tvSeeMore.visibility = View.VISIBLE
                    lblContent.maxLines = 3
                    lblContent.ellipsize = TextUtils.TruncateAt.END
                }
            }
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