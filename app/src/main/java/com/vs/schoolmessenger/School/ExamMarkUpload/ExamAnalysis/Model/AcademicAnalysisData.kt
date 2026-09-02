package com.vs.schoolmessenger.School.ExamMarkUpload.ExamAnalysis.Model

data class AcademicAnalysisData(
    val meta: Meta,
    val subjects: List<SubjectAcademicData>,
    val students: List<SubjectAcademicData>,
    val `class`: ClassData,
    val subjectStats: List<SubjectStat>,
    val insights: List<InsightAcademicData>
)