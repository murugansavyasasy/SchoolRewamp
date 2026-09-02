package com.vs.schoolmessenger.School.ExamMarkUpload.ExamAnalysis.Model

data class ClassGrade (
    val grade: String,
    val min: String,
    val max: String,
    val count: String,
    val studentIds: List<String>
)