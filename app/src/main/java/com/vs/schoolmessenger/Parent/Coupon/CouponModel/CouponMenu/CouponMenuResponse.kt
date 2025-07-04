package com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponMenu

import com.google.gson.annotations.SerializedName

data class CouponMenuResponse(
    @SerializedName("data") val data: CouponMenuData?
)