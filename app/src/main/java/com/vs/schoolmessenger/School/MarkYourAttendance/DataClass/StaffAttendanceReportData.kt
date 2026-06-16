package com.vs.schoolmessenger.School.MarkYourAttendance.DataClass

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

data class StaffAttendanceReportData(
    @SerializedName(APIKeyNames.name) val name: String,
    @SerializedName(APIKeyNames.staff_id) val staff_id: String,
    @SerializedName(APIKeyNames.date) val date: String,
    @SerializedName(APIKeyNames.role) val role: String,
    @SerializedName(APIKeyNames.leave_type) val leave_type: String,
    @SerializedName(APIKeyNames.attendance_type) val attendance_type: Map<String, String>,
    @SerializedName(APIKeyNames.in_time) val in_time: String,
    @SerializedName(APIKeyNames.out_time) val out_time: String,
    @SerializedName(APIKeyNames.working_hours) val working_hours: String
)

