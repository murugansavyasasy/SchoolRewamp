package com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketActivateCoupon

import com.google.gson.annotations.SerializedName

class ActivateCouponData {
    // Getters for all fields
    @SerializedName("coupons")
    val coupons: MutableList<ActivateCoupon?>? = null

    @SerializedName("merchant_logo")
    val merchant_logo: String? = null

    @SerializedName("offer")
    val offer: String? = null

    @SerializedName("redirect_url")
    val redirect_url: String? = null

    @SerializedName("coupon_code")
    val coupon_code: String? = null

    @SerializedName("isCTAvalid")
    val isCTAvalid: Boolean = false

    @SerializedName("CTAname")
    val cTAname: String? = null

    @SerializedName("CTAredirect")
    val cTAredirect: String? = null
}
