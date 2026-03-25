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
    val total_amount: Int,
    val paid_amount: Int,
    val pending_amount: Int,
    val discount: Int,
    val status: String
)

data class Payment(
    val paid_amount: Int,
    val paid_date: String,
    val payment_mode: String
)