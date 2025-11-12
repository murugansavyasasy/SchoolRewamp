package com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.GetAttendanceDetails

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.StudentAttendanceReportData

class StudentAttendanceReport (
    @SerializedName(APIKeyNames.holiday_message) val holiday_message: String,
    @SerializedName(APIKeyNames.attd_report) val attd_report: List<StudentAttendanceReportData>
)