package com.vs.schoolmessenger.School.Communication

import com.google.gson.annotations.SerializedName

data class TextSendResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<Any> // Empty list, or you could define a more specific data type if needed
)
