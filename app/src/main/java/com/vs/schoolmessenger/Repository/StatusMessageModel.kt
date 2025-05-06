package com.vs.schoolmessenger.Repository

import com.google.gson.annotations.SerializedName

data class StatusMessageModel(
    @SerializedName(APIKeyNames.status) val status: Boolean,
    @SerializedName(APIKeyNames.message) val message: String,
    @SerializedName(APIKeyNames.data) val data: List<Any>
)
