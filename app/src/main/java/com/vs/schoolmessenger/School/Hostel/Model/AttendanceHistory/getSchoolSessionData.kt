package com.vs.schoolmessenger.School.Hostel.Model.AttendanceHistory

class getSchoolSessionData (
    val session_type_id: Int,
    val session_name: String?="Morning",
    val session_presentCount: Int?=5,
    val session_absentCount: Int?=5,
    val students: List<getHAStudentData>
)