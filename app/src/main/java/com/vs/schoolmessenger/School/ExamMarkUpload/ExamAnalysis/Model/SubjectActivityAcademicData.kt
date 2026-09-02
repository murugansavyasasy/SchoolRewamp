package com.vs.schoolmessenger.School.ExamMarkUpload.ExamAnalysis.Model

data class SubjectActivityAcademicData (
    val activityId: String,
    val name: String,
    val maxMarks: String,
    val averageMark: String,
    val averagePercentage: String,
    val rubrics: List<AverageRubric>
)