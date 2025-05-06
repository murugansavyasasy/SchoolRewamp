package com.vs.schoolmessenger.School.MarkYourAttendance.DataClass

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

data class PunchHistoryData(
    @SerializedName(APIKeyNames.date) val date: String,
    @SerializedName(APIKeyNames.timings) val timings: List<PunchTimingsData>
)