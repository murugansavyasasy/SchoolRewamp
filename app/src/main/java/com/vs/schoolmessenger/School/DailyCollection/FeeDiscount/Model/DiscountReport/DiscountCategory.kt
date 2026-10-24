package com.vs.schoolmessenger.School.DailyCollection.FeeDiscount.Model.DiscountReport

import com.google.gson.annotations.SerializedName

data class DiscountCategory(
    @SerializedName("category") val category: String? = null,
    @SerializedName("count") val count: String? = null,
    @SerializedName("type_name") val type_name: String? = null,
    @SerializedName("total") val total: String? = null,
    @SerializedName("discount_data") val discountData: List<DiscountItem> = emptyList()
)