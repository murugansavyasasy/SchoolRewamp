package com.vs.schoolmessenger.School.ExamReview.ApiResponseModel

data class ExamSummary(
    val exam_id: String,
    val label: String,
    val total: String,
    val percentage: String
)