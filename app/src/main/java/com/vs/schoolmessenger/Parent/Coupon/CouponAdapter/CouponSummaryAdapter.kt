package com.vs.schoolmessenger.Parent.Coupon.CouponAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.Parent.Coupon.CouponFragment.HomeFragment
import com.vs.schoolmessenger.Parent.Coupon.CouponListener.CouponSummaryClickListener
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponSummary.CampaignItem
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class CouponSummaryAdapter(
    private val itemList: List<CampaignItem>,
    private val listener: CouponSummaryClickListener,
    private val context: HomeFragment,
    private val isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.item_coupon)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_coupon, parent, false)
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


        private val lblProductName: TextView = itemView.findViewById(R.id.lblProductName)
        private val lblProductOffer: TextView = itemView.findViewById(R.id.lblProductOffer)
        private val lblCompanyName: TextView = itemView.findViewById(R.id.lblCompanyName)
        private val lblDays: TextView = itemView.findViewById(R.id.lblDays)
        private val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        private val imgOverlay: ImageView = itemView.findViewById(R.id.imgOverlay)

        fun bind(data: CampaignItem, position: Int) {

            lblProductName.text = data.categoryName
            lblProductOffer.text = "${data.discount ?: "0"}% Off"
            lblCompanyName.text = data.merchantName


            val expiryDateStr: String = data.expiryDate

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            try {
                val expiryDate = sdf.parse(expiryDateStr)
                val currentDate = Date()

                val diffInMillies = expiryDate!!.getTime() - currentDate.getTime()
                val daysLeft = TimeUnit.MILLISECONDS.toDays(diffInMillies)

                if (daysLeft >= 0) {
                    lblDays.text =(daysLeft.toString() + " days")
                } else {
                    lblDays.text = "Expired"
                }
            } catch (e: ParseException) {
                e.printStackTrace()
                lblDays.text = "Invalid date"
            }

            Glide.with(context)
                .load(data.thumbnail)
                .into(imgProduct)

            Glide.with(context)
                .load(data.merchantLogo)
                .into(imgOverlay)

            itemView.setOnClickListener {
                listener.onSummaryClick(data)
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}