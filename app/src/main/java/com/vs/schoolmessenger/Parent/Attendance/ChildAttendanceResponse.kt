package com.vs.schoolmessenger.Parent.Attendance

import com.vs.schoolmessenger.School.SchoolStrength.SchoolData

data class ChildAttendanceResponse (
    val status: Boolean,
    val message: String,
    val data: List<AttendanceReportStudentData>
)