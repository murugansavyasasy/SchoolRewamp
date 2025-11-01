package com.vs.schoolmessenger.Parent.Communication

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

data class VoiceData(
    @SerializedName(APIKeyNames.type) val type: String?,
    @SerializedName(APIKeyNames.id) val id: String?,
    @SerializedName(APIKeyNames.header_id) val header_id: String?,

    @SerializedName(APIKeyNames.content) val content: String?,
    @SerializedName(APIKeyNames.title) var title: String?,
    @SerializedName(APIKeyNames.date) val date: String?,
    @SerializedName(APIKeyNames.time) var time: String?,
    @SerializedName(APIKeyNames.duration) var duration: String?,
    @SerializedName(APIKeyNames.subject) var subject: String?,
    @SerializedName(APIKeyNames.is_unread) var is_unread: Boolean? = null,
    @SerializedName(APIKeyNames.is_emergency) var is_emergency: Boolean? = null,
    @SerializedName(APIKeyNames.is_archive) var is_archive: Boolean? = null
)



