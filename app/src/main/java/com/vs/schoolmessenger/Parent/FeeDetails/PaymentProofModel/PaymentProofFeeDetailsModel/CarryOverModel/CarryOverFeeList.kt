package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.CarryOverModel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CarryOverFeeList (
    val fee_group_type_id: String? = null,
    val fee_group_type_name: String? = null,
    val fee_id: String? = null,
    val fee_name: String? = null,
    val fee_amount: String? = null,
    val paid_amount: String? = null,
    val pending_amount: String? = null,
    val discount_amount: String? = null,
): Parcelable