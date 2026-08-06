package com.vs.schoolmessenger.School.ExamReview.Model

data class SubjectAverage(
    val subject: String,
    val subjectCode: String,
    val average: Double,
    val averagePercentage: Int,
    val grade: String
)
