package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel

import com.vs.schoolmessenger.School.StudentDetails.StudentDetails

data class PaymentProofData (
    val student_details: PaymentProofStudentDetails?,
    val payment_details: List<PaymentDetails>?
)