package com.vs.schoolmessenger.Parent.Coupon.CouponModel.TicketActivateCoupon

import com.google.gson.annotations.SerializedName

class ActivateCouponResponse {
    @SerializedName("status")
    val isStatus: Boolean = false

    @SerializedName("message")
    val message: String? = null

    @SerializedName("data")
    val data: ActivateCouponData? = null
}
