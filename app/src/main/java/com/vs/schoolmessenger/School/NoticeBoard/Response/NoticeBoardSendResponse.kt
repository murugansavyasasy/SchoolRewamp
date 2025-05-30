package com.vs.schoolmessenger.School.NoticeBoard.Response

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

data class NoticeBoardSendResponse (
    @SerializedName(APIKeyNames.status) val status: Boolean,
    @SerializedName(APIKeyNames.message) val message: String,
    @SerializedName(APIKeyNames.data) val data: List<Any>
)
