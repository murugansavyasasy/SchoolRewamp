package com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponMenu

import com.google.gson.annotations.SerializedName

class Category {
    @SerializedName("id")
    var id: Int = 0


    @SerializedName("category_name")
    var categoryName: String? = null

    @SerializedName("category_image")
    var categoryImage: String? = null


    var drawableResId: Int = -1
}