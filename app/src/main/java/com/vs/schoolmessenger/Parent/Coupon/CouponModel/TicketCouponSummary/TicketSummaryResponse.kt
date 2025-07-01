package com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketCouponSummary

import com.google.gson.annotations.SerializedName

class TicketSummaryResponse {
    @SerializedName("status")
    val isStatus: Boolean = false

    @SerializedName("message")
    val message: String? = null

    @SerializedName("data")
    val data: TicketSummaryData? = null
}

