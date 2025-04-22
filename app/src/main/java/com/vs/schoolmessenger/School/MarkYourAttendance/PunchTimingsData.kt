package com.vs.schoolmessenger.School.MarkYourAttendance

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.ResponseKeys

data class PunchTimingsData(
    @SerializedName(ResponseKeys.time) val time: String,
    @SerializedName(ResponseKeys.device_model) val device_model: String,
    @SerializedName(ResponseKeys.device_id) val device_id: String,
    @SerializedName(ResponseKeys.punch_type) val punch_type: PunchTypes
)



