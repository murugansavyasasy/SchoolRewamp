package com.vs.schoolmessenger.School.AbsenteesReport.Model

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.School.AbsenteesReport.Model.Student

data class AbsenteeStudentsResponse (
    @SerializedName(APIKeyNames.status) val status: Boolean,
    @SerializedName(APIKeyNames.message) val message: String,
    @SerializedName(APIKeyNames.data) val data: List<Student>
)


