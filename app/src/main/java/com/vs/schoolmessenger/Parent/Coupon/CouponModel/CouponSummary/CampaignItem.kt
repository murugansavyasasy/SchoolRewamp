package com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponSummary

data class CampaignItem(
    val temp_id: Int,
    val source_link: String,
    val campaign_name: String,
    val campaign_type: String,
    val threshold_amount: String?,
    val offer_text: String?,
    val thumbnail: String,
    val expiry_date: String,
    val end_date: String,
    val offer_type: String,
    val discount: Int,
    val merchant_name: String,
    val category_name: String,
    val category_image: String,
    val merchant_logo: String,
    val coupon_status: String,
    val coupon_code: String,
    val isCTAvalid: Boolean,
    val CTAname: String,
    val CTAredirect: String,
    val offer_to_show: String
)
