package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data

data class StudentMarkApi(
    val student_id: String,
    val student_name: String,
    val gender: String,
    val roll_no: String,
    val admission_no: String,
    val marks: List<SubjectMark>
)

