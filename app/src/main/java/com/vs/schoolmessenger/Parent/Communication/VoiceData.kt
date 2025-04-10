package com.vs.schoolmessenger.Parent.Communication

import com.google.gson.annotations.SerializedName

data class VoiceData(
    @SerializedName("type") val type: String,
    @SerializedName("detail_id") val detail_id: String,
    @SerializedName("content") val content: String,
    @SerializedName("description") var description: String,
    @SerializedName("date") val date: String,
    @SerializedName("time") var time: String,
    @SerializedName("subject") var subject: String,
    @SerializedName("app_unread_status") var app_unread_status: Boolean
)
