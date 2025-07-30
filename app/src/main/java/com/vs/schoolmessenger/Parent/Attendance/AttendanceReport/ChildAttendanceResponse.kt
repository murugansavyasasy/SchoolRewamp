package com.vs.schoolmessenger.Parent.Attendance.AttendanceReport

data class ChildAttendanceResponse (
    val status: Boolean,
    val message: String,
    val data: List<AttendanceReportStudentData>
)