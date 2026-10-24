package com.vs.schoolmessenger.School.DailyCollection.FeeDiscount.Model.DiscountReport

import com.google.gson.annotations.SerializedName

data class DiscountReportData(
    @SerializedName("discount_details") val discountDetails: List<DiscountCategory> = emptyList(),
    @SerializedName("total_count") val totalCount: String? = null,
    @SerializedName("total_discount") val totalDiscount: String? = null,
    @SerializedName("total_subsidy") val total_subsidy: String? = null,
    @SerializedName("student_data_count") val student_data_count: String? = null,
    @SerializedName("total_discount_and_subsidy") val total_discount_and_subsidy: String? = null,
)