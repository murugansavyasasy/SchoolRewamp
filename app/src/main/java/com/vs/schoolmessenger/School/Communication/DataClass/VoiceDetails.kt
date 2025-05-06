package com.vs.schoolmessenger.School.Communication.DataClass

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.ResponseKeys

data class VoiceDetails(@SerializedName(ResponseKeys.status) val status: Boolean,
                        @SerializedName(ResponseKeys.message) val message: String,
                        @SerializedName(ResponseKeys.data) val data: List<VoiceHistoryDetails>)
