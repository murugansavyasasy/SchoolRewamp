package com.vs.schoolmessenger.Parent.Coupon.CouponView.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponSummary.Summary
import com.vs.schoolmessenger.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class CouponSummaryAdapter(
    private val context: Context,
    summaryList: MutableList<Summary>,
    pointsRemaining: Int,
    pointPerCoupon: Int
) : RecyclerView.Adapter<CouponSummaryAdapter.ViewHolder?>() {
    private var summaryList: MutableList<Summary>
    private val pointsRemaining: Int
    private val pointPerCoupon: Int


    init {
        this.summaryList = summaryList
        this.pointsRemaining = pointsRemaining
        this.pointPerCoupon = pointPerCoupon
    }

    fun updateList(newList: MutableList<Summary>) {
        summaryList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_coupon, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val summary: Summary = summaryList.get(position)

        holder.lblProductName.setText(summary.categoryName)
        holder.lblProductOffer.setText(summary.discount)
        holder.lblCompanyName.setText(summary.merchantName)

        val expiryDateStr: String? = summary.expiry_date
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        try {
            val expiryDate = sdf.parse(expiryDateStr)
            val currentDate = Date()

            val diffInMillies = expiryDate!!.getTime() - currentDate.getTime()
            val daysLeft = TimeUnit.MILLISECONDS.toDays(diffInMillies)

            if (daysLeft >= 0) {
                holder.lblDays.setText(daysLeft.toString() + " days")
            } else {
                holder.lblDays.setText("Expired")
            }
        } catch (e: ParseException) {
            e.printStackTrace()
            holder.lblDays.setText("Invalid date")
        }

        Glide.with(context)
            .load(summary.thumbnail)
            .into(holder.imgProduct)

        Glide.with(context)
            .load(summary.merchantLogo)
            .into(holder.imgOverlay)

        holder.header.setOnClickListener(View.OnClickListener { view: View? ->
//            val intent: Intent = Intent(context, BottomSheetActivity::class.java)
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//            intent.putExtra("category_name", summary.getCategoryName())
//            intent.putExtra("discount", summary.getDiscount())
//            intent.putExtra("merchant_name", summary.getMerchantName())
//            intent.putExtra("thumbnail", summary.getThumbnail())
//            intent.putExtra("source_link", summary.getSource_link())
//            intent.putExtra("coupon_status", summary.getCoupon_status())
//            intent.putExtra("merchant_logo", summary.getMerchantLogo())
//            intent.putExtra("points_percoupon", pointPerCoupon)
//            Log.d("points_percoupon", pointPerCoupon.toString())
//            intent.putExtra("points_remaining", pointsRemaining)
//            Log.d("points_remaining", pointsRemaining.toString())
//            context.startActivity(intent)
        })
    }

    override fun getItemCount(): Int {
        return summaryList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgProduct: ImageView
        var imgUser: ImageView
        var imgOverlay: ImageView
        var lblProductName: TextView
        var lblProductOffer: TextView
        var lblCompanyName: TextView
        var lblDays: TextView
        var header: LinearLayout
        var isProgressBarImage: ProgressBar?


        init {
            imgUser = itemView.findViewById<ImageView?>(R.id.imgUser)
            imgProduct = itemView.findViewById<ImageView?>(R.id.imgProduct)
            imgOverlay = itemView.findViewById<ImageView?>(R.id.imgOverlay)
            lblProductName = itemView.findViewById<TextView?>(R.id.lblProductName)
            lblCompanyName = itemView.findViewById<TextView?>(R.id.lblCompanyName)
            lblProductOffer = itemView.findViewById<TextView?>(R.id.lblProductOffer)
            lblDays = itemView.findViewById<TextView?>(R.id.lblDays)
            header = itemView.findViewById<LinearLayout?>(R.id.header)
            isProgressBarImage = itemView.findViewById<ProgressBar?>(R.id.isProgressBarImage)
        }
    }
}
