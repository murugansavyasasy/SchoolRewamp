package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data

data class InvalidMarkIssue(
    val studentName: String,
    val subjectName: String,
    val enteredValue: String
)
