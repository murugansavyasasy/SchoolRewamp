package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data

data class StudentMarkList(
    val name: String,
    val student_id: String,
    val gender: String,
    val rollNo: String,
    val admission_no: String,
    val marks: MutableList<Double?>,
    val markTexts: MutableList<String>,          // extracted / editable
    val mockMarkTexts: MutableList<String>,
    val isEditList: MutableList<Boolean>
)

