package com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponSummaryFilter

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponSummary.CouponSummaryData

class CouponSummaryFilterResponse {
    @SerializedName("status")
    val isStatus: Boolean = false

    @SerializedName("message")
    val message: String? = null

    @SerializedName("data")
    val data: CouponSummaryData? = null
}
