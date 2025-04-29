package com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass

import com.google.gson.annotations.SerializedName

data class GetFilePathDetails(
    @SerializedName("type")
    val type: String,
    @SerializedName("path")
    val path: String,
)
