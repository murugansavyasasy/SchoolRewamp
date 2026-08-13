package com.vs.schoolmessenger.School.ExamReview.ApiResponseModel

data class SubjectExamAnalysis (
    val subject_id: String,
    val subject_name: String,
    val marks: List<MarkExamAnaysis>
)