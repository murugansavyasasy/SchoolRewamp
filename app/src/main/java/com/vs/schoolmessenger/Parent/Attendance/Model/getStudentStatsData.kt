package com.vs.schoolmessenger.Parent.Attendance.Model

data class getStudentStatsData(
    val total_working_days: Int,
    val present_days: Int,
    val absent_days: Int,
    val completed_working_days: Int,
    val upcoming_working_days: Int,
    val attendance_percentage: String,
    val weekly_status: getWeeekStatusData
)

data class getWeeekStatusData(
    val start: String,
    val end: String,
    val student_name: String,
    val att_list: List<String>,
)