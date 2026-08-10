package com.vs.schoolmessenger.School.ExamReview.ApiResponseModel

data class MarkExamAnaysis (
    val exam_name: String,
    val obtained_mark: String,
    val max_mark: String,
    val attendance: String,
    val remarks: String,
    val activity_name: String,
    val exam_date: String,
    val session: String,
    val min_mark: String,
    val syllabus: String,
    val is_publish: String
)