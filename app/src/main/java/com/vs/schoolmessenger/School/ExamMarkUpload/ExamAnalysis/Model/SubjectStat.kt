package com.vs.schoolmessenger.School.ExamMarkUpload.ExamAnalysis.Model

data class SubjectStat (
    val subjectId: String,
    val name: String,
    val maxMarks: String,
    val passCount: String,
    val passPercentage: String,
    val average: String,
    val highest: String,
    val lowest: String,
    val sd: String,
    val above: String,
    val below: String,
    val bands: List<SubjectBand>,
    val grades: List<SubjectGrade>,
    val activities: List<SubjectActivityAcademicData>,
    val ranked: List<RankedStudent>,
    val bySection: List<SectionStat>
)