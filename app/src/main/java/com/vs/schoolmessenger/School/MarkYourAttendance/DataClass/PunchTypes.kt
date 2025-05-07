package com.vs.schoolmessenger.School.MarkYourAttendance.DataClass

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

data class PunchTypes(
    @SerializedName(APIKeyNames.id) val id: Int,
    @SerializedName(APIKeyNames.value) val value: String

)