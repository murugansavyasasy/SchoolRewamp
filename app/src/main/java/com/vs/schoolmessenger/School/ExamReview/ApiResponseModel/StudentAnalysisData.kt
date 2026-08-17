package com.vs.schoolmessenger.School.ExamReview.ApiResponseModel

data class StudentAnalysisData (
    val exam_series: List<String>,
    val subjects: List<SubjectExamAnalysis>,
    val trend: List<TrendExamAnalysis>,
    val summary: SummaryExamAnalysis
)