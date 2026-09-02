package com.vs.schoolmessenger.School.ExamMarkUpload.ExamAnalysis.Model

data class Meta (
    val examId: String,
    val examName: String,
    val instituteName: String,
    val standard: String,
    val passPercent: String,
    val gradeBands: List<GradeBand>,
    val sections: List<String>,
    val studentCount: String,
    val subjectCount: String,
    val totalMax: String,
    val maxMarksSource: String,
    val generatedAt: String
)