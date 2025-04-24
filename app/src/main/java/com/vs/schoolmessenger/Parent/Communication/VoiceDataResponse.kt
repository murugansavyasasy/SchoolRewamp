package com.vs.schoolmessenger.Parent.Communication

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.ResponseKeys

data class VoiceDataResponse(
    @SerializedName(ResponseKeys.status) val status: Boolean,
    @SerializedName(ResponseKeys.message) val message: String,
    @SerializedName(ResponseKeys.data) val data: List<VoiceData>
)
