package com.vs.schoolmessenger.School.StudentDetails.Model.FeeOverview.PaymentHistory

data class PaymentHistory(
    val paymentId: String?,
    val paid_date: String?,
    val pending_amount: Int?,
    val paid_amount: Int?,
    val amount_given: Int?,
    val discount_amount: Int?,
    val in_progress: Int?,
    val payment_mode: String?
)