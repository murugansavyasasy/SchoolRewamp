package com.vs.schoolmessenger.School.StudentDetails.Model.FeeOverview.ExamPerformance

data class ExamPerformance(
    val improvementPercentage: Double?,
    val highestScore: Int?,
    val highestExamName: String?,
    val exams: List<Exam>?
)