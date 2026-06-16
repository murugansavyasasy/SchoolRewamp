package com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponMenu

import com.google.gson.annotations.SerializedName

data class Category(
    @SerializedName("id") val id: Int?,
    @SerializedName("category_name") val categoryName: String?,
    @SerializedName("category_image") val categoryImage: String?,
    var drawableResId: Int = -1
)