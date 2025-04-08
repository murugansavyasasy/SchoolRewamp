package com.vs.schoolmessenger.School.Communication

import com.google.gson.annotations.SerializedName

data class VoiceDetails(@SerializedName("status") val status: Boolean,
                        @SerializedName("message") val message: String,
                        @SerializedName("data") val data: List<VoiceHistoryDetails>)
