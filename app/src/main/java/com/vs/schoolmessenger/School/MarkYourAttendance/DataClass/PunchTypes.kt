package com.vs.schoolmessenger.School.MarkYourAttendance.DataClass

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.ResponseKeys

data class PunchTypes(
    @SerializedName(ResponseKeys.id) val id: Int,
    @SerializedName(ResponseKeys.value) val value: String

)