package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel

data class PaymentDetails (
    val total_amount: String?,
    val ai_detected_amount: String?,
    val user_enter_amount: String?,
    val fee_details: PaymentProofFeeDetails?,
    val is_payment_validated: String?,
    val validated_by: String?,
    val validated_on: String?,
    val payment_id: String?,
    val proof_uploaded: List<ProofUploaded>?,
    val created_on: String?,
    val created_by: String?,
    val remarks: String?,
    val proof_details: List<ProofDetails>?
)