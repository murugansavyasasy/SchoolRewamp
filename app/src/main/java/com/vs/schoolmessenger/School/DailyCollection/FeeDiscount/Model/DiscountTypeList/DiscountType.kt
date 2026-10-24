package com.vs.schoolmessenger.School.DailyCollection.FeeDiscount.Model.DiscountTypeList

import com.google.gson.annotations.SerializedName

data class DiscountType(
    @SerializedName("id") val id: String? = null,
    @SerializedName("discount_name") val discountName: String? = null,
    @SerializedName("type_name") val type_name: String? = null,
    @SerializedName("type") val type: String? = null
)