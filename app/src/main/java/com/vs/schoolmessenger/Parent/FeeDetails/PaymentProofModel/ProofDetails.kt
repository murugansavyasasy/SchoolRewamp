package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel

data class ProofDetails(
    val payment_status: String,
    val receipt_type: String,
    val payment_method: String,
    val upi_provider: String?,
    val receipt_date: String?,
    val validation_message: String?,
    val transaction_id: String?,
    val reference_number: String?,
    val paid_amount: String?,
    val receipt_time: String?,
    val bank_name: String?,
    val payer_name: String?,
    val payee_name: String?,
    val upi_id: String?,
    val account_number: String?,
    val failure_reason: String?,
    val confidence: Int
)