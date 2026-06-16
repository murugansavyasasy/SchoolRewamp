package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data

data class MarkColumn(
    val subjectId: String,
    val subjectName: String,
    val activityId: String,
    val activityName: String,
    val selected_name: String,
    val maxMark: Int
)
