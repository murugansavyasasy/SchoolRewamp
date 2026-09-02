package com.vs.schoolmessenger.School.ExamMarkUpload.ExamAnalysis.Model

data class ScoreBand (
    val label: String,
    val min: String,
    val max: String,
    val count: String,
    val studentIds: List<String>
)