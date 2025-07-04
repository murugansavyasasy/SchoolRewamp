package com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponSummary

import com.google.gson.annotations.SerializedName

data class CampaignItem(
    @SerializedName("temp_id") val tempId: Int,
    @SerializedName("source_link") val sourceLink: String,
    @SerializedName("campaign_name") val campaignName: String,
    @SerializedName("campaign_type") val campaignType: String,
    @SerializedName("threshold_amount") val thresholdAmount: String?,
    @SerializedName("offer_text") val offerText: String?,
    val thumbnail: String,
    @SerializedName("expiry_date") val expiryDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("offer_type") val offerType: String,
    val discount: Int,
    @SerializedName("merchant_name") val merchantName: String,
    @SerializedName("category_name") val categoryName: String,
    @SerializedName("category_image") val categoryImage: String,
    @SerializedName("merchant_logo") val merchantLogo: String,
    @SerializedName("offer_to_show") val offerToShow: String,

    @SerializedName("customer_buys_value") val customerBuysValue: String? = null,
    @SerializedName("customer_gets_value") val customerGetsValue: String? = null,
    @SerializedName("old_combo_offer") val oldComboOffer: String? = null
)
