package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data

data class MarkResponse(
    val status: Boolean,
    val message: String,
    val data: List<StudentMarkApi>
)
