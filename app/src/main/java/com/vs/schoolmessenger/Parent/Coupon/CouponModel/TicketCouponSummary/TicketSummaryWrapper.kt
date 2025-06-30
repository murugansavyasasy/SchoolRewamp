package com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketCouponSummary

import com.google.gson.annotations.SerializedName

class TicketSummaryWrapper {
    @SerializedName("current_page")
    val currentPage: Int = 0

    @SerializedName("data")
    val data: MutableList<TicketSummary?>? = null
}
