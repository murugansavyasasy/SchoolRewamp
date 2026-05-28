package com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponMenu

import com.google.gson.annotations.SerializedName

data class CouponMenuData(
    @SerializedName("categories") val categories: List<Category>?
)