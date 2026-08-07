package com.vs.schoolmessenger.School.ExamReview.Model

data class Summary(
    val examsAnalysed: Int,
    val averageTotal: Double,
    val averagePercentage: Double,
    val overallGrade: String,
    val bestExam: ExamRef,
    val worstExam: ExamRef
)