package com.vs.schoolmessenger.School.MarkYourAttendance.DataClass

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

data class StaffLocationData(
    @SerializedName(APIKeyNames.latitude) val latitude: String,
    @SerializedName(APIKeyNames.longitude) val longitude: String,
    @SerializedName(APIKeyNames.location) val location: String,
    @SerializedName(APIKeyNames.distance) val distance: String
)