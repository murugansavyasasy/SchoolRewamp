package com.vs.schoolmessenger.School.Communication.DataClass

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

data class VoiceDetails(@SerializedName(APIKeyNames.status) val status: Boolean,
                        @SerializedName(APIKeyNames.message) val message: String,
                        @SerializedName(APIKeyNames.data) val data: List<VoiceHistoryDetails>)
