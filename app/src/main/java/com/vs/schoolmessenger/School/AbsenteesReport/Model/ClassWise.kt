package com.vs.schoolmessenger.School.AbsenteesReport.Model


data class ClassWise(
    val id: String,
    val name: String,
    val total_absentees: String,
    val date: String? = null,
    val section_wise: List<SectionWise>
)