package com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.ResponseKeys

data class GetFilePathDetails(
    @SerializedName(ResponseKeys.type)
    val type: String,
    @SerializedName(ResponseKeys.path)
    val path: String,
)
