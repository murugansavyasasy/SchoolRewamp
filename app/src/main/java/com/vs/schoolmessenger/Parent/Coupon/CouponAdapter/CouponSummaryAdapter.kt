package com.vs.schoolmessenger.Parent.Coupon.CouponAdapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.Parent.Coupon.CouponFragment.HomeFragment
import com.vs.schoolmessenger.Parent.Coupon.CouponListener.CouponSummaryClickListener
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponSummary.CampaignItem
import com.vs.schoolmessenger.Parent.Coupon.CouponView.CouponActivateActivity
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
    private val isLoading: Boolean,
    private val earnedPoints: Int,
    private val spentPoints: Int,
    private val remainingPoints: Int,
    private val pointspercoupon: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>(), Filterable {

    private var fullList: List<CampaignItem> = itemList ?: listOf()
    private var filteredList: List<CampaignItem> = itemList ?: listOf()

    init {
        fullList = itemList ?: listOf()
        filteredList = fullList
    }

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
            holder.bind(filteredList[position], position)
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""
                val result = if (query.isEmpty()) {
                    fullList
                } else {
                    fullList.filter {
                        it.category_name.lowercase().contains(query) ||
                                it.merchant_name.lowercase().contains(query) ||
                                it.campaign_name.lowercase().contains(query)

                    }
                }
                return FilterResults().apply { values = result }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<CampaignItem> ?: listOf()
                listener.onSearchResultEmpty(filteredList.isEmpty())
                notifyDataSetChanged()
            }
        }
    }


    override fun getItemCount(): Int {
        return if (isLoading) 10 else filteredList.size
    }


    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        private val lblProductName: TextView = itemView.findViewById(R.id.lblProductName)
        private val lblProductOffer: TextView = itemView.findViewById(R.id.lblProductOffer)
        private val lblCompanyName: TextView = itemView.findViewById(R.id.lblCompanyName)
        private val lblDays: TextView = itemView.findViewById(R.id.lblDays)
        private val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        private val imgOverlay: ImageView = itemView.findViewById(R.id.imgOverlay)

        private val header: LinearLayout = itemView.findViewById(R.id.header)

        fun bind(data: CampaignItem, position: Int) {

            lblProductName.text = data.category_name
            lblProductOffer.text = "${data.discount ?: "0"}% Off"
            lblCompanyName.text = data.merchant_name


            val expiryDateStr: String = data.expiry_date

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            try {
                val expiryDate = sdf.parse(expiryDateStr)
                val currentDate = Date()

                val diffInMillies = expiryDate!!.time - currentDate.time
                val daysLeft = TimeUnit.MILLISECONDS.toDays(diffInMillies)

                if (daysLeft >= 0) {
                    lblDays.text = (daysLeft.toString() + " days")
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
                .load(data.merchant_logo)
                .into(imgOverlay)

            itemView.setOnClickListener {
                listener.onSummaryClick(data)
            }

            header.setOnClickListener {
                val intent = Intent(itemView.context, CouponActivateActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent.putExtra("category_name", data.category_name)
                intent.putExtra("discount", data.discount)
                intent.putExtra("merchant_name", data.merchant_name)
                intent.putExtra("thumbnail", data.thumbnail)
                intent.putExtra("source_link", data.source_link)
                intent.putExtra("coupon_status", data.coupon_status)
//                Log.d("coupon_status",data.coupon_status)
                intent.putExtra("merchant_logo", data.merchant_logo)
                intent.putExtra("earnedPoints", earnedPoints)
                intent.putExtra("spentPoints", spentPoints)
                intent.putExtra("remainingPoints", remainingPoints)
                intent.putExtra("pointspercoupon", pointspercoupon)
                itemView.context.startActivity(intent)
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}