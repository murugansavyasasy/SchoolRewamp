package com.vs.schoolmessenger.School.ExamReview.AnalysisSetResponseModel

data class AnalysisSetsResponse (
    val status: Boolean,
    val message: String,
    val data: List<AnalysisSet>
)