package com.vs.schoolmessenger.School.Hostel.Model.AttendanceHistory

class getSchoolSessionData (
    val session_type_id: Int,
    val session_name: String,
    val present_count: String,
    val absent_count: String,
    val students: List<getHAStudentData>
)