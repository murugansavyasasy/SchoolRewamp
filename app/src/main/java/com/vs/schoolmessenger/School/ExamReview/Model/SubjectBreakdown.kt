package com.vs.schoolmessenger.School.ExamReview.Model

data class SubjectBreakdown(
    val subject: String,
    val subjectCode: String,
    val maxMarks: Int,
    val average: Double,
    val averagePercentage: Int,
    val grade: String,
    val examWise: List<ExamWiseMark>
)