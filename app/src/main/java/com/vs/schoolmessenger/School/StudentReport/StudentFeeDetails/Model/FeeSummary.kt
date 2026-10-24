package com.vs.schoolmessenger.School.StudentReport.StudentFeeDetails.Model

import com.google.gson.annotations.SerializedName

data class FeeSummary(
    @SerializedName("total_amount")
    val totalAmount: String? = null,

    @SerializedName("total_paid")
    val totalPaid: String? = null,

    @SerializedName("total_discount")
    val totalDiscount: String? = null,

    @SerializedName("total_pending")
    val totalPending: String? = null
)