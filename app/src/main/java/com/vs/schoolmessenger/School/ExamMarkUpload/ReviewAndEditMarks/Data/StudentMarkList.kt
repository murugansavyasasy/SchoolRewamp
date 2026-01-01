package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data

data class StudentMarkList(
    val name: String,
    val student_id: String,
    val rollNo: String,
    val marks: MutableList<Int?>,
    val markTexts: MutableList<String>,          // extracted / editable
    val mockMarkTexts: MutableList<String>       // mock reference
)

