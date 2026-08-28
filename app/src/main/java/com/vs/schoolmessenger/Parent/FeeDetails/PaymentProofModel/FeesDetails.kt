package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel

data class FeesDetails(
    val fee_id: String?,
    val fee_name: String?,
    val fee_amount: String?,
    val discount_availed: String?,
    val actual_paid: String?,
    val amount_to_be_paid: String?
)