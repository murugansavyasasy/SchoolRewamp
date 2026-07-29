package com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard

data class FeeDetails(
    val fee_id: Int,
    val fee_name: String,
    val fee_group: String,
    val hostel_details: HostelFeeDetails,
    val summary: FeeSummary,
    val payments: List<Payment>
)

data class HostelFeeDetails(
    val hostel_name: String,
    val room_no: String,
    val bed_no: String
)

data class FeeSummary(
    val total_amount: String,
    val paid_amount: String,
    val pending_amount: String,
    val discount: String,
    val status: String
)

data class Payment(
    val paid_amount: String,
    val paid_date: String,
    val payment_mode: String
)