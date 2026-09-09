package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.OthersModel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GenericFee (
    val term_group_type_id: String? = null,
    val fee_id: String? = null,
    val fee_name: String? = null,
    val discount_availed: String? = null,
    val actual_paid: String? = null,
    val amount_to_be_paid: String? = null,
    val month_details: List<Month_details>? = null

): Parcelable