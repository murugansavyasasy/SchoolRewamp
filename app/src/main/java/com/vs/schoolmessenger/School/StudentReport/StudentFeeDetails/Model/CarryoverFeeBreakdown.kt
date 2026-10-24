package com.vs.schoolmessenger.School.StudentReport.StudentFeeDetails.Model

import com.google.gson.annotations.SerializedName

data class CarryoverFeeBreakdown(
    @SerializedName("discount_amount_record")
    val discountAmountRecord: String? = null,

    @SerializedName("fee_group_type_id")
    val feeGroupTypeId: String? = null,

    @SerializedName("fee_group_type_name")
    val feeGroupTypeName: String? = null,

    @SerializedName("fee_id")
    val feeId: String? = null,

    @SerializedName("fee_name")
    val feeName: String? = null,

    @SerializedName("m_feeamount")
    val feeAmount: String? = null,

    @SerializedName("paid_amount")
    val paidAmount: String? = null,

    @SerializedName("paid_amount_record")
    val paidAmountRecord: String? = null,

    @SerializedName("pending_amount")
    val pendingAmount: String? = null,

    @SerializedName("spot_discount")
    val spotDiscount: String? = null
)