package com.vs.schoolmessenger.School.Communication

import com.google.gson.annotations.SerializedName

data class VoiceHistoryDetails(
    @SerializedName("file_path") val filePath: String,
    @SerializedName("url") val url: String,
    @SerializedName("description") val description: String,
    @SerializedName("sent_on") val sentOn: String,
    @SerializedName("school_id") val schoolId: String,
    @SerializedName("header_id") val headerId: String,
    @SerializedName("duration") val duration: Int
)
