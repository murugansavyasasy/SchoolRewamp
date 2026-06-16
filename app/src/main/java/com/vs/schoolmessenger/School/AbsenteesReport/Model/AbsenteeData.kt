package com.vs.schoolmessenger.School.AbsenteesReport.Model

data class AbsenteeData(
    val date: String,
    val day: String,
    val absent_date_only: String,
    val total_absentees: String,
    val class_wise: List<ClassWise>
)