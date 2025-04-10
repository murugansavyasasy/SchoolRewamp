package com.vs.schoolmessenger.Parent.Communication

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds

data class VoiceDataResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<VoiceData>
)
