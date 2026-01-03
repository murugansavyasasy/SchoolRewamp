package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data

data class MaxMarkIssue(
    val studentName: String,
    val subjectName: String,
    val activityName: String,
    val enteredMark: String,
    val maxMark: Int
)

