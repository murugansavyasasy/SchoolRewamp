package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data

data class StudentMarkApi(
    val student_id: String,
    val student_name: String,
    val marks: List<SubjectMark>
)

