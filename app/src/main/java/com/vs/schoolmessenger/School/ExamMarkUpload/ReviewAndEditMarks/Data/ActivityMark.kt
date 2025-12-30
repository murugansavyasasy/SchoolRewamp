package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data

data class ActivityMark(
    val id: String,
    val name: String,
    var mark: String,      // editable
    val max_mark: String
)
