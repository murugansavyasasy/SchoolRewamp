package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Model

data class SaveMarksModel(
    val status: Boolean,
    val message: String,
    val data: List<Any>
)