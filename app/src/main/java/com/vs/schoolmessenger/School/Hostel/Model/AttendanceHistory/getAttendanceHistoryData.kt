package com.vs.schoolmessenger.School.Hostel.Model.AttendanceHistory

data class getAttendanceHistoryData(
    val date: String,
    val year: String,
    val attendancePercentage: Int,
    val totalStudents: Int,
    val presentStudents: Int,
    val absentStudents: Int,
    val roomsMarked: Int,
    val totalRooms: Int
)