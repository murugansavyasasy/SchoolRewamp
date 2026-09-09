package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.OthersModel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Month_details (
    val month_name: String? = null,
    val month_id: String? = null,
    val amount_per_month: String? = null,
    val discount_amount: String? = null,
    val paid: String? = null,
    val amount_to_be_paid: String? = null,
    val pending: String? = null,
    val is_paid: String? = null,

): Parcelable