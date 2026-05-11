package com.vs.schoolmessenger.Parent.FeeDetails.Model

data class OnlinePaymentResponse(
    val status: Boolean,
    val message: String,
    val data: List<OnlinePaymentData>
)
