package com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model


data class getActivitySubjectNameData(
    val subject: String,
    val paper: List<getActivityPaperNameData>,
    var isSelected: Boolean = false

)