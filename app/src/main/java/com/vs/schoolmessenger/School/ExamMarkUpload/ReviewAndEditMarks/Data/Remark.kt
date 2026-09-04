package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data

data class Remark(
    val reference_type: String,
    val mark: String,
    val is_edit: Boolean
)