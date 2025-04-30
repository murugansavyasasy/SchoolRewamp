package com.vs.schoolmessenger.School.MarkYourAttendance.DataClass

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.ResponseKeys

data class LocationHistoryResponse(
    @SerializedName(ResponseKeys.status) val status: Boolean,
    @SerializedName(ResponseKeys.message) val message: String,
    @SerializedName(ResponseKeys.data) val data: List<LocationHistoryData>
)