package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel

data class PaymentProofResponse (
    val status: Boolean?,
    val message: String?,
    val data: List<PaymentProofData>?
)