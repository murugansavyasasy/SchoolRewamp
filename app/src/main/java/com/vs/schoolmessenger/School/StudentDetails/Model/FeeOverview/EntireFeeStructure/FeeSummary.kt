package com.vs.schoolmessenger.School.StudentDetails.Model.FeeOverview.EntireFeeStructure

data class FeeSummary(
    val total_amount: Int?,
    val total_paid: Int?,
    val total_discount: Int?,
    val total_pending: Int?,
    val paymentProgress: Int?
)