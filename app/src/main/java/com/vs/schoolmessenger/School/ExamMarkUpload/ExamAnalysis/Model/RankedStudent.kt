package com.vs.schoolmessenger.School.ExamMarkUpload.ExamAnalysis.Model

data class RankedStudent (
    val rank: String,
    val studentId: String,
    val name: String,
    val section: String,
    val mark: String,
    val percentage: String,
    val grade: String,
    val pass: Boolean
)