package com.vs.schoolmessenger.School.ExamMarkUpload.ExamAnalysis.Model

data class SubjectAcademicData (
    val subjectId: String,
    val name: String,
    val maxMarks: String,
    val activities: List<ActivityAcademicData>
)