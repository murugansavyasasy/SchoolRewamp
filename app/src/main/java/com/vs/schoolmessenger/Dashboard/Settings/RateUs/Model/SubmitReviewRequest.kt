package com.vs.schoolmessenger.Dashboard.Settings.RateUs.Model

data class SubmitReviewRequest(
    val mobile_number: String,
    val rating: Int,
    val description: String
)
