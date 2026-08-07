package com.vs.schoolmessenger.School.ExamReview.Model

data class StatCards(
    val averageScore: String,
    val averagePercentage: String,
    val overallGrade: String,
    val bestExamTotal: String,
    val bestExamPercentage: String,
    val worstExamTotal: String,
    val worstExamPercentage: String,
    val examsCount: Int,
    val subjectsCount: Int,
    val maxMarksPerSubject: Int
)