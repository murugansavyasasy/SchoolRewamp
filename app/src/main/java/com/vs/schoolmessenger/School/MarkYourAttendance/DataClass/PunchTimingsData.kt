package com.vs.schoolmessenger.School.MarkYourAttendance.DataClass

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

data class PunchTimingsData(
    @SerializedName(APIKeyNames.time) val time: String,
    @SerializedName(APIKeyNames.device_model) val device_model: String,
    @SerializedName(APIKeyNames.device_id) val device_id: String,
    @SerializedName(APIKeyNames.punch_type) val punch_type: PunchTypes
)