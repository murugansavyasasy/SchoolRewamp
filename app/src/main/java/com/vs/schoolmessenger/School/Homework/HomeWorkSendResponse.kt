package com.vs.schoolmessenger.School.Homework

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.ResponseKeys

data class HomeWorkSendResponse
    (
    @SerializedName(ResponseKeys.status) val status: Boolean,
@SerializedName(ResponseKeys.message) val message: String,
@SerializedName(ResponseKeys.data) val data: List<Any> // Empty list, or you could define a more specific data type if needed
)
