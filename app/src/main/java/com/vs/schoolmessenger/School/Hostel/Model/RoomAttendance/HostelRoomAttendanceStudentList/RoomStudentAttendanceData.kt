package com.vs.schoolmessenger.School.Hostel.Model.RoomAttendance.HostelRoomAttendanceStudentList
data class RoomStudentAttendanceData(
    val id: String,
    val name: String,
    val admission_no: String,
    val roll_no: String,
    val gender: String,
    val class_id: String,
    val class_name: String,
    val section_id: String,
    val section_name: String,
    val primary_mobile: String,
    var status: String,
    var outpass_id: String,
    var out_date: String,
    var in_date: String,
    var reason: String,
    var outpass_status: String,
    )
