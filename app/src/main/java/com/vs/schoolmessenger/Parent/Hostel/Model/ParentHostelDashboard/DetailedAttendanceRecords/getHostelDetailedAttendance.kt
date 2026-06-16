package com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard.DetailedAttendanceRecords

data class getHostelDetailedAttendance(
    val sessions: List<String>,
    val days: List<DayAttendance>
)