package com.vs.schoolmessenger.School.StudentReport.StudentFeeDetails.Model

import com.google.gson.annotations.SerializedName

data class QuantityFee(
    @SerializedName("fee_name")
    val feeName: String? = null,

    @SerializedName("actual_amount")
    val actualAmount: String? = null,

    @SerializedName("paid_amount")
    val paidAmount: String? = null,

    @SerializedName("paid_details")
    val paidDetails: List<FeePaidDetail>? = null
)