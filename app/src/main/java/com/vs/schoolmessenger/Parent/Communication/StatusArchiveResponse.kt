package com.vs.schoolmessenger.Parent.Communication

import com.google.gson.annotations.SerializedName

data class StatusArchiveResponse (
    @SerializedName("status") val status: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<Any>
)
