package com.vs.schoolmessenger.School.SchoolStrength.Model

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

data class SchoolStrengthResponse(
    @SerializedName(APIKeyNames.status)val status: Boolean,
    @SerializedName(APIKeyNames.message) val message: String,
    @SerializedName(APIKeyNames.data) val data: List<SchoolData>
)