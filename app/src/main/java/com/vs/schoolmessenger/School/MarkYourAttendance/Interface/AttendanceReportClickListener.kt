package com.vs.schoolmessenger.School.MarkYourAttendance.Interface

import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.StaffAttendanceReportData

interface AttendanceReportClickListener {
    fun onItemClick(data: StaffAttendanceReportData)

}