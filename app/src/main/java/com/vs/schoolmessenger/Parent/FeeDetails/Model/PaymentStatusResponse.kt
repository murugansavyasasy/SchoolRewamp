package com.vs.schoolmessenger.Parent.FeeDetails.Model

data class PaymentStatusResponse(
    val status: Boolean,
    val message: String,
    val data: List<PaymentStatusData>
)
