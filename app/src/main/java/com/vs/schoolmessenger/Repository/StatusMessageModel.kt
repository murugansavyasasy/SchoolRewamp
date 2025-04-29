package com.vs.schoolmessenger.Repository

import com.google.gson.annotations.SerializedName

data class StatusMessageModel(
    @SerializedName(ResponseKeys.status) val status: Boolean,
    @SerializedName(ResponseKeys.message) val message: String,
    @SerializedName(ResponseKeys.data) val data: List<Any>
)
