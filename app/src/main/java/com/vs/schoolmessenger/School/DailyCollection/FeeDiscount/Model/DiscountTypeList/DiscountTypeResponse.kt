package com.vs.schoolmessenger.School.DailyCollection.FeeDiscount.Model.DiscountTypeList

import com.google.gson.annotations.SerializedName

data class DiscountTypeResponse(
    @SerializedName("status") val status: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<DiscountType> = emptyList()
)