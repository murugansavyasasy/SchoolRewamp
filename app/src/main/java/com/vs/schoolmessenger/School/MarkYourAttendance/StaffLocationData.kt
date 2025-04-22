package com.vs.schoolmessenger.School.MarkYourAttendance

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.ResponseKeys

data class StaffLocationData(
    @SerializedName(ResponseKeys.latitude) val latitude: String,
    @SerializedName(ResponseKeys.longitude) val longitude: String,
    @SerializedName(ResponseKeys.location) val location: String,
    @SerializedName(ResponseKeys.distance) val distance: String
)
