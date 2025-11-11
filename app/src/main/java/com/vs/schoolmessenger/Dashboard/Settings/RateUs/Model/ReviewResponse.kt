package com.vs.schoolmessenger.Dashboard.Settings.RateUs.Model

data class ReviewResponse (
    val status: Boolean,
    val message: String,
    val data: List<ReviewData>
)