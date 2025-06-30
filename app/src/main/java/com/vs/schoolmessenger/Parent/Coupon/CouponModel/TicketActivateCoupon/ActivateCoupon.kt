package com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketActivateCoupon

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ActivateCoupon : Serializable {
    // Getters
    @SerializedName("coupon_code")
    val coupon_code: String? = null

    @SerializedName("qr_code")
    val qr_code: String? = null

    @SerializedName("expiry_date")
    val expiry_date: String? = null

    @SerializedName("merchant_logo")
    val merchant_logo: String? = null

    @SerializedName("offer")
    val offer: String? = null

    @SerializedName("redirect_url")
    val redirect_url: String? = null

    @SerializedName("isCTAvalid")
    val isCTAvalid: String? = null

    @SerializedName("CTAname")
    val cTAname: String? = null

    @SerializedName("CTAredirect")
    val cTAredirect: String? = null
}
