package com.vs.schoolmessenger.School.StudentReport.StudentFeeDetails.Model

import com.google.gson.annotations.SerializedName

data class TransportFee(
    @SerializedName("actual_amount")
    val actualAmount: String? = null,

    @SerializedName("paid_amount")
    val paidAmount: String? = null,

    @SerializedName("discount_amount")
    val discountAmount: String? = null,

    @SerializedName("pending_amount")
    val pendingAmount: String? = null,

    @SerializedName("paid_details")
    val paidDetails: List<FeePaidDetail>? = null
)