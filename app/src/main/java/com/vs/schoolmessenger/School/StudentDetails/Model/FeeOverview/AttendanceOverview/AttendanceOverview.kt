package com.vs.schoolmessenger.School.StudentDetails.Model.FeeOverview.AttendanceOverview

import com.vs.schoolmessenger.School.StudentDetails.Model.FeeOverview.AttendanceOverview.WeeklyAttendance

data class AttendanceOverview(
    val attendancePercentage: Int?,
    val presentDays: Int?,
    val absentDays: Int?,
    val leaveDays: Int?,
    val weeklyAttendance: List<WeeklyAttendance>?
)