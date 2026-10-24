package com.vs.schoolmessenger.School.StudentReport.StudentFeeDetails.Model

import com.google.gson.annotations.SerializedName

data class CarryoverFee(
    @SerializedName("year_name")
    val yearName: String? = null,

    @SerializedName("fee_type")
    val feeType: String? = null,

    @SerializedName("actual_amount")
    val actualAmount: String? = null,

    @SerializedName("paid_amount")
    val paidAmount: String? = null,

    @SerializedName("discount_amount")
    val discountAmount: String? = null,

    @SerializedName("pending_amount")
    val pendingAmount: String? = null,

    @SerializedName("break_down")
    val breakDown: List<CarryoverFeeBreakdown>? = null
)