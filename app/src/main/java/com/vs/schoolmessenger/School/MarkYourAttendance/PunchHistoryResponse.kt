package com.vs.schoolmessenger.School.MarkYourAttendance

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.ResponseKeys

data class PunchHistoryResponse(@SerializedName(ResponseKeys.status) val status: Boolean,
                                @SerializedName(ResponseKeys.message) val message: String,
                                @SerializedName(ResponseKeys.data) val data: List<PunchHistoryData>)
