package com.vs.schoolmessenger.School.ExamReview.Model

data class TrendItem(
    val examId: String,
    val label: String,
    val total: Int,
    val percentage: Double,
    val grade: String
)