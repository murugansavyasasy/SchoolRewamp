package com.vs.schoolmessenger.School.ExamReview.Model

data class MarksTableItem(
    val examId: String,
    val label: String,
    val subjects: List<MarksSubject>,
    val total: Int,
    val percentage: Double,
    val grade: String
)