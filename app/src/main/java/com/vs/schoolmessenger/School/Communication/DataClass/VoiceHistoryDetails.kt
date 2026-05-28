package com.vs.schoolmessenger.School.Communication.DataClass

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

data class VoiceHistoryDetails(
    @SerializedName(APIKeyNames.file_path) val filePath: String,
    @SerializedName(APIKeyNames.url) val url: String,
    @SerializedName(APIKeyNames.title) val title: String,
    @SerializedName(APIKeyNames.sent_on) val sentOn: String,
    @SerializedName(APIKeyNames.school_id) val schoolId: String,
    @SerializedName(APIKeyNames.header_id) val headerId: String,
    @SerializedName(APIKeyNames.duration) val duration: Int
)
