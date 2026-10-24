package com.vs.schoolmessenger.School.DailyCollection.FeeDiscount.Model.DiscountReport

import com.google.gson.annotations.SerializedName

data class DiscountReportResponse(
    @SerializedName("status") val status: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<DiscountReportData> = emptyList()
)