package com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

data class GetFilePathDetails(
    @SerializedName(APIKeyNames.type)
    val type: String,
    @SerializedName(APIKeyNames.path)
    val path: String,
)
