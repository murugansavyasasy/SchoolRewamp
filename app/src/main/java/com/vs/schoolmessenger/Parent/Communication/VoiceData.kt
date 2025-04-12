package com.vs.schoolmessenger.Parent.Communication

import com.google.gson.annotations.SerializedName

data class VoiceData(
    @SerializedName("type") val type: String,
    @SerializedName("id") val id: String,
    @SerializedName("content") val content: String,
    @SerializedName("description") var description: String,
    @SerializedName("date") val date: String,
    @SerializedName("time") var time: String,
    @SerializedName("subject") var subject: String,
    @SerializedName("is_unread") var is_unread: Boolean,
    @SerializedName("is_archive") var is_archive: Boolean
)
