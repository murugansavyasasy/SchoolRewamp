package com.vs.schoolmessenger.School.Communication

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.RequestKeys
import com.vs.schoolmessenger.Repository.ResponseKeys

data class VoiceHistoryDetails(
    @SerializedName(RequestKeys.file_path) val filePath: String,
    @SerializedName(RequestKeys.url) val url: String,
    @SerializedName(ResponseKeys.title) val title: String,
    @SerializedName(ResponseKeys.sent_on) val sentOn: String,
    @SerializedName(ResponseKeys.school_id) val schoolId: String,
    @SerializedName(ResponseKeys.header_id) val headerId: String,
    @SerializedName(ResponseKeys.duration) val duration: Int
)
