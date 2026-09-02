package com.vs.schoolmessenger.School.ExamMarkUpload.ExamAnalysis.Model

data class InsightAcademicData (
    val id: String,
    val severity: String,
    val tag: String,
    val title: String,
    val detail: String,
    val students: List<String>,
    val action: String
)