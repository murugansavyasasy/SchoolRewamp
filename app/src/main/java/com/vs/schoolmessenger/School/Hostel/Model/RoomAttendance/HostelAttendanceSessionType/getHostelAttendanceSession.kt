package com.vs.schoolmessenger.School.Hostel.Model.RoomAttendance.HostelAttendanceSessionType

data class getHostelAttendanceSession (
    val status: Boolean,
    val message: String,
    val data: List<SessionData>
)