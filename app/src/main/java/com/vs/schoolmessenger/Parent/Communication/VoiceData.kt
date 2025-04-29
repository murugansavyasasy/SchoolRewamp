package com.vs.schoolmessenger.Parent.Communication

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.ResponseKeys

data class VoiceData(
    @SerializedName(ResponseKeys.type) val type: String?,
    @SerializedName(ResponseKeys.id) val id: String?,
    @SerializedName(ResponseKeys.content) val content: String?,
    @SerializedName(ResponseKeys.title) var title: String?,
    @SerializedName(ResponseKeys.date) val date: String?,
    @SerializedName(ResponseKeys.time) var time: String?,
    @SerializedName(ResponseKeys.subject) var subject: String?,
    @SerializedName(ResponseKeys.is_unread) var is_unread: Boolean? = null,
    @SerializedName(ResponseKeys.is_archive) var is_archive: Boolean? = null
)



