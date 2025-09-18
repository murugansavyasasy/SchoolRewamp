package com.vs.schoolmessenger.School.AbsenteesReport.Model


data class ClassWise(
    val class_id: String,
    val class_name: String,
    val total_absentees: String,
    val student_counts: String,
    val date: String? = null,
    val section_wise: List<SectionWise>
)