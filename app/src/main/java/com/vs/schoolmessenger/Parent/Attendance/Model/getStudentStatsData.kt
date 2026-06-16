package com.vs.schoolmessenger.Parent.Attendance.Model

data class getStudentStatsData(
    val total_working_days: Double,
    val present_days: Double,
    val absent_days: Double,
    val completed_working_days: Double,
    val upcoming_working_days: Double,
    val attendance_percentage: String,
    val weekly_status: getWeeekStatusData
)

data class getWeeekStatusData(
    val start: String,
    val end: String,
    val student_name: String,
    val att_list: List<String>,
)