package com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel

data class StudentAttendanceReportData(
    val student_name: String,
    var admission_no: String,
    val att_status: String,
    val absent_on: String
)
