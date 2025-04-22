package com.vs.schoolmessenger.School.MarkYourAttendance

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.ResponseKeys

data class StaffAttendanceReportData(
    @SerializedName(ResponseKeys.name) val name: String,
    @SerializedName(ResponseKeys.date) val date: String,
    @SerializedName(ResponseKeys.leave_type) val leave_type: String,
    @SerializedName(ResponseKeys.attendance_type) val attendance_type: String,
    @SerializedName(ResponseKeys.in_time) val in_time: String,
    @SerializedName(ResponseKeys.out_time) val out_time: String,
    @SerializedName(ResponseKeys.working_hours) val working_hours: String
)
