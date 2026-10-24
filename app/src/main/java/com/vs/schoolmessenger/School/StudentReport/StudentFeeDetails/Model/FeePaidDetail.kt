package com.vs.schoolmessenger.School.StudentReport.StudentFeeDetails.Model

import com.google.gson.annotations.SerializedName

data class FeePaidDetail(
    @SerializedName("paid_date")
    val paidDate: String? = null,

    @SerializedName("pending_amount")
    val pendingAmount: String? = null,

    @SerializedName("amount_given")
    val amountGiven: String? = null,

    @SerializedName("paid_amount")
    val paidAmount: String? = null,

    @SerializedName("discount_amount")
    val discountAmount: String? = null,

    @SerializedName("payment_mode")
    val paymentMode: String? = null,

    @SerializedName("in_progress")
    val inProgress: String? = null
)
