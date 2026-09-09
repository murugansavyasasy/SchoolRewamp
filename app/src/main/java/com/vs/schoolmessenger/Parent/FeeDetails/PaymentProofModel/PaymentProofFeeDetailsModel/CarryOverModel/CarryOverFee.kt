package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.CarryOverModel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CarryOverFee (
    val id: String? = null,
    val fee_name: String? = null,
    val fee_category_name: String? = null,
    val fee_category: String? = null,
    val fee_group_type_name: String? = null,
    val pending_carried_over: String? = null,
    val discount_amount: String? = null,
    val paid_amount: String? = null,
    val amount_to_be_paid: String? = null,
    val carry_over_fee: List<CarryOverFeeList>? = null

): Parcelable