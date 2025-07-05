package com.vs.schoolmessenger.Parent.Coupon.CouponAdapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filter.FilterResults
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.Parent.Coupon.CouponView.MycouponViewActivity
import com.vs.schoolmessenger.Parent.Coupon.CouponListener.TicketCouponClickListener
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponSummary.CampaignItem
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketCouponSummary.TicketSummary
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil

class TicketCouponAdapter(
    private val itemList: List<TicketSummary>,
    private val listener: TicketCouponClickListener,
    private val context: Context,
    private val isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private var fullList: List<TicketSummary> = itemList ?: listOf()
    private var filteredList: List<TicketSummary> = itemList ?: listOf()

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
                        (it.merchant_name?.lowercase()?.contains(query) == true) ||
                                (it.campaign_name?.lowercase()?.contains(query) == true)
                    }

                }
                return FilterResults().apply { values = result }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<TicketSummary> ?: listOf()
                listener.onSearchResultEmpty(filteredList.isEmpty())
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 10 else filteredList.size
    }

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val centerImage: ImageView = itemView.findViewById(R.id.center_image)
        private val categoryName: TextView = itemView.findViewById(R.id.categoryname)
        private val discount: TextView = itemView.findViewById(R.id.discount)
        private val merchantName: TextView = itemView.findViewById(R.id.merchantname)
        private val couponStatus: TextView = itemView.findViewById(R.id.couponstatus)

        private val card_view: CardView = itemView.findViewById(R.id.card_view)

        fun bind(data: TicketSummary, position: Int) {
            categoryName.text = data.merchant_name
            discount.text = data.offer_to_show
            merchantName.text = "Expires in ${data.expires_in} days"

            Log.d(
                "TicketCouponAdapter",
                "Loading image for position $position: '${data.merchant_logo}'"
            )

            if (!data.merchant_logo.isNullOrEmpty()) {
                Glide.with(itemView.context).load(data.merchant_logo)
                    .placeholder(R.drawable.allimage).error(R.drawable.allimage).into(centerImage)
                Log.w(
                    "TicketCouponAdapter++", "merchant_logo is null or empty for position $position"
                )

            } else {
                centerImage.setImageResource(R.drawable.allimage)
                Log.w(
                    "TicketCouponAdapter++", "merchant_logo is null or empty for position $position"
                )
            }

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


            card_view.setOnClickListener {
                val intent = Intent(context, MycouponViewActivity::class.java)
                val locationList: MutableList<TicketSummary.Location?> =
                    data.location_list ?: mutableListOf()
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra("merchant_name", data.merchant_name)
                intent.putExtra("offer_to_show", data.offer_to_show)
                intent.putExtra("how_to_use", data.how_to_use)
                intent.putExtra("coupon_code", data.coupon_code)
                intent.putExtra("cover_image", data.cover_image)
                intent.putExtra("expiry_date", data.expiry_date)
                intent.putExtra("expiry_type", data.expiry_type)
                intent.putExtra("merchant_logo", data.merchant_logo)
                intent.putExtra("location_list", ArrayList(locationList))
                context.startActivity(intent)
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
