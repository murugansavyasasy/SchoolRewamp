package com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.GetAttendanceDetails.StudentAttendanceReport

data class StudentAttendanceReportDataResponse(
    @SerializedName(APIKeyNames.status) val status: Boolean,
    @SerializedName(APIKeyNames.message) val message: String,
    @SerializedName(APIKeyNames.data) val data: List<StudentAttendanceReport>
)

