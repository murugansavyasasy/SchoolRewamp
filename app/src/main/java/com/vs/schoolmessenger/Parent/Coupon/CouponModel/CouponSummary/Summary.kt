package com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponSummary

import com.google.gson.annotations.SerializedName

class Summary {
    @SerializedName("expiry_date")
    var expiry_date: String? = null

    @SerializedName("source_link")
    val source_link: String? = null

    @SerializedName("campaign_name")
    val campaignName: String? = null

    @SerializedName("campaign_type")
    val campaignType: String? = null

    @SerializedName("thumbnail")
    val thumbnail: String? = null

    @SerializedName("offer_type")
    val offerType: String? = null

    @SerializedName("discount")
    val discount: Int = 0

    @SerializedName("merchant_name")
    val merchantName: String? = null

    @SerializedName("category_name")
    val categoryName: String? = null

    @SerializedName("category_image")
    val categoryImage: String? = null

    @SerializedName("merchant_logo")
    val merchantLogo: String? = null

    @SerializedName("coupon_status")
    val coupon_status: String? = null
}