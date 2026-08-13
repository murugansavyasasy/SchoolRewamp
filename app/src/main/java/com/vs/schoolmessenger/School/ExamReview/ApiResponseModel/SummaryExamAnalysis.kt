package com.vs.schoolmessenger.School.ExamReview.ApiResponseModel

data class SummaryExamAnalysis (
    val exams_analysed: String,
    val average_total: String,
    val average_percentage: String,
    val best_exam: BestExam,
    val worst_exam: WorstExam
)