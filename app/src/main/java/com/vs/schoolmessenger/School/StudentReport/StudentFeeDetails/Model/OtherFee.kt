package com.vs.schoolmessenger.School.StudentReport.StudentFeeDetails.Model

import com.google.gson.annotations.SerializedName

data class OtherFee(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("fee_name")
    val feeName: String? = null,

    @SerializedName("term_name")
    val termName: String? = null,

    @SerializedName("term_id")
    val termId: String? = null,

    @SerializedName("actual_amount")
    val actualAmount: String? = null,

    @SerializedName("paid_amount")
    val paidAmount: String? = null,

    @SerializedName("discount_amount")
    val discountAmount: String? = null,

    @SerializedName("pending_amount")
    val pendingAmount: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("paid_details")
    val paidDetails: List<FeePaidDetail>? = null
)