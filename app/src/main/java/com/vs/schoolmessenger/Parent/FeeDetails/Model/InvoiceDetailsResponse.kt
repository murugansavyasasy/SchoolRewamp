package com.vs.schoolmessenger.Parent.FeeDetails.Model

data class InvoiceDetailsResponse(
    val status: Boolean,
    val message: String,
    val data: List<String>
)
