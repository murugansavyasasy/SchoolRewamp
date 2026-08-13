package com.vs.schoolmessenger.School.ExamReview.AnalysisSetResponseModel

data class AnalysisSet (
    val id: String,
    val setName: String,
    val class_tests: List<ClassTestAnalysisSet>
)