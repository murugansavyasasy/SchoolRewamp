package com.vs.schoolmessenger.School.ClassTest.Review.Model

import com.vs.schoolmessenger.Parent.FeeDetails.Model.PaymentStatusData

data class CreateClassTestResponse (
    val status: Boolean,
    val message: String,
    val data: Any
)