package com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketActivateCouponSummary

import com.google.gson.annotations.SerializedName

class ActivateCouponSummaryResponse {
    @SerializedName("status")
    val isStatus: Boolean = false

    @SerializedName("message")
    val message: String? = null

    @SerializedName("data")
    val data: ActivateCouponSummaryData? = null
}