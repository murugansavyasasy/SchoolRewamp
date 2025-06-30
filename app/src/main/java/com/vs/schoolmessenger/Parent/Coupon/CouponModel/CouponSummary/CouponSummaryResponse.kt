package com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponSummary

import com.google.gson.annotations.SerializedName

class CouponSummaryResponse {
    @SerializedName("status")
    val isStatus: Boolean = false

    @SerializedName("message")
    val message: String? = null

    @SerializedName("data")
    val data: CouponSummaryData? = null
}
