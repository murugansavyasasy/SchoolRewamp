package com.vs.schoolmessenger.School.AttendanceReportFromStaff

data class AttendanceReportFromStaffDataClass(
    val status: Boolean,
    val message: String,
    val data: List<AttendanceStaffDataClass>
)

data class AttendanceStaffDataClass(
    val overall_stat: OverallStatDataClass,
    val all_attd: Map<String, DateAttendanceDataClass> // KEY = "28-02-2025"
)

data class OverallStatDataClass(
    val present: Int,
    val absent: Int,
    val not_marked: Int
)

data class DateAttendanceDataClass(
    val stat: StatDataClass,
    val attd_details: List<AttendanceDetailDataClass>
)

data class StatDataClass(
    val present: Int,
    val absent: Int,
    val not_marked: Int
)

data class AttendanceDetailDataClass(
    val staff_id: String,
    val name: String,
    val designation: String,
    val role: String,
    val attendance_type: Map<String, String>, // FD -> Present/Absent
    val in_time: String,
    val out_time: String,
    val working_hours: String
)
