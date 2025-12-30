package com.vs.schoolmessenger.School.ExamMarkUpload.ReviewAndEditMarks.Data

data class StudentMarkList(
    val name: String,
    val rollNo: String,

    // Parsed numeric value (used for calculations)
    val marks: MutableList<Int?>,

    // Raw text entered by teacher (used for UI restore)
    val markTexts: MutableList<String>
)
