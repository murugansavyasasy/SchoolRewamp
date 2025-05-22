package com.vs.schoolmessenger.Parent.Attendance
data class AttendanceReportStudentData(
    val student_name: String,
    var admission_no: String,
    val att_status: String,
    val absent_on: String
)
