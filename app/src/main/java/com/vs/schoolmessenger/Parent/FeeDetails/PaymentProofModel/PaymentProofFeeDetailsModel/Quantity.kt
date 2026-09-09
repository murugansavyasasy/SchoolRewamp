package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Quantity(
    val id: String?,
    val fee_name: String?,
    val institute_fee_group_type_id: String?,
    val uom: String?,
    val uom_price: String?,
    val qom_given: String?,
    val amount_to_be_paid: String?,
    val previous_quantity: String?,
    val previous_quantity_amount: String?
): Parcelable