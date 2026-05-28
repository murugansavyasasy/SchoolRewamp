package com.vs.schoolmessenger.Parent.Coupon.CouponModel.LogactiveApiResponse

import com.google.gson.annotations.SerializedName

class LogActiveApiResponse {
    @SerializedName("status")
    var status: Int = 0

    @SerializedName("message")
    var message: String? = null
}
