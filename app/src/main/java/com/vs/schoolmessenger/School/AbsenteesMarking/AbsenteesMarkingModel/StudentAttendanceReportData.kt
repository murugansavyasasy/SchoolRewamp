package com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel

data class StudentAttendanceReportData(
    val student_name: String,
    var admission_no: String,
    var profile: String,
    var roll_no: String,
    var gender: String,
    val att_status: String,
    val absent_on: String
)
