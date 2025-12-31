package com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model


data class getActivitySubjectNameData(
    val section_id: String,
    val section_name: String,
    val class_id: String,
    val class_name: String,
    val subject_id: String,
    val subject: String,
    val paper: List<getActivityPaperNameData>,
    var isSelected: Boolean = false

)