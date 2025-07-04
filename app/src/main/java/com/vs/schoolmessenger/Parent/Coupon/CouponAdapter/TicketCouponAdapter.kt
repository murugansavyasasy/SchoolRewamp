package com.vs.schoolmessenger.Parent.Coupon.CouponAdapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.Parent.Coupon.CouponAdapter.CouponMenuAdapter.ShimmerViewHolder
import com.vs.schoolmessenger.Parent.Coupon.CouponListener.TicketCouponClickListener
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketCouponSummary.TicketSummary
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil

class TicketCouponAdapter(
    private val itemList: List<TicketSummary>,
    private val listener: TicketCouponClickListener,
    private val context: Context,
    private val isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.ticket_couponsummary)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.ticket_couponsummary, parent, false)
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList[position], position)
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 10 else itemList.size
    }

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val centerImage: ImageView = itemView.findViewById(R.id.center_image)
        private val categoryName: TextView = itemView.findViewById(R.id.categoryname)
        private val discount: TextView = itemView.findViewById(R.id.discount)
        private val merchantName: TextView = itemView.findViewById(R.id.merchantname)
        private val couponStatus: TextView = itemView.findViewById(R.id.couponstatus)

        fun bind(data: TicketSummary, position: Int) {
            // Set basic info
            categoryName.text = data.merchant_name
            discount.text = data.offer_to_show
            merchantName.text = "Expires in ${data.expires_in} days"

            // Debug log for image URL
            Log.d("TicketCouponAdapter", "Loading image for position $position: '${data.merchant_logo}'")

            // Load image using Glide with error/placeholder handling
            if (!data.merchant_logo.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(data.merchant_logo)
                    .placeholder(R.drawable.allimage)
                    .error(R.drawable.allimage)
                    .into(centerImage)
                Log.w("TicketCouponAdapter++", "merchant_logo is null or empty for position $position")

            } else {
                centerImage.setImageResource(R.drawable.allimage)
                Log.w("TicketCouponAdapter++", "merchant_logo is null or empty for position $position")
            }

            // Set status
            when (data.coupon_status) {
                "activated" -> {
                    couponStatus.visibility = View.GONE
                }
                "claimed" -> {
                    couponStatus.visibility = View.VISIBLE
                    couponStatus.background =
                        ContextCompat.getDrawable(context, R.drawable.redeemed_backgroundgreen)
                    couponStatus.text = "Redeemed"
                }
                "expired" -> {
                    couponStatus.visibility = View.VISIBLE
                    couponStatus.background =
                        ContextCompat.getDrawable(context, R.drawable.redeemed_backgroundgrey)
                    couponStatus.text = "Expired"
                }
                else -> {
                    couponStatus.visibility = View.GONE
                }
            }


        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
