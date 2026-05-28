package com.vs.schoolmessenger.Parent.FeeDetails.Model

data class OnlinePaymentData(
    val id: String,
    val student_id: String,
    val total_amount: String,
    val order_status: String,
    val order_status_log: Map<String, Any>,
    val order_id: String,
    val created_on: String,
    val order_status_update_on: String
)
