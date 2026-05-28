package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data

data class StudentMarkRow(
    val rollNo: Int,
    val studentId: String,
    val studentName: String,
    val activities: MutableList<ActivityMark>
)
