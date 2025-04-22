package com.vs.schoolmessenger.School.MarkYourAttendance

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.ResponseKeys

data class PunchHistoryData(
    @SerializedName(ResponseKeys.date) val date: String,
    @SerializedName(ResponseKeys.timings) val timings: List<PunchTimingsData>
)
