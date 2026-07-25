package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Model

data class Rubric(
    val id: String,
    val name: String,
    val mark: String,
    val is_edit: Boolean,
    val max_mark: String,
    val selected_name: String
)
