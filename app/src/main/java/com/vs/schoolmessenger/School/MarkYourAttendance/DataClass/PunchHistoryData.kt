package com.vs.schoolmessenger.School.MarkYourAttendance.DataClass

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.ResponseKeys
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.PunchTimingsData

data class PunchHistoryData(
    @SerializedName(ResponseKeys.date) val date: String,
    @SerializedName(ResponseKeys.timings) val timings: List<PunchTimingsData>
)