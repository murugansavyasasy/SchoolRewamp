package com.vs.schoolmessenger.School.ExamMarkUpload.ExamAnalysis.Model

data class AcademicAnalysisResponse(
    val status: Boolean,
    val message: String,
    val data: List<AcademicAnalysisData>
)
