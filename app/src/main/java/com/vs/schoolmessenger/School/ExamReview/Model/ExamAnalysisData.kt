package com.vs.schoolmessenger.School.ExamReview.Model

data class ExamAnalysisData(
    val student: Student,
    val summary: Summary,
    val statCards: StatCards,
    val subjectBreakdown: List<SubjectBreakdown>,
    val trendData: List<TrendItem>,
    val marksTable: List<MarksTableItem>,
    val subjectAverages: List<SubjectAverage>,
    val meta: Meta
)