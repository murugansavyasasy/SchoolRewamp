package com.vs.schoolmessenger.Parent.Hostel.Model.DetailedAttendanceRecords

data class getHostelDetailedAttendance(
    val sessions: List<String>,
    val days: List<DayAttendance>
)