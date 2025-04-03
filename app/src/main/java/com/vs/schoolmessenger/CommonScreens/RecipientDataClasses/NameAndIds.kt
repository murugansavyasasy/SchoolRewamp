package com.vs.schoolmessenger.CommonScreens.RecipientDataClasses

import com.google.gson.annotations.SerializedName

data class NameAndIds(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)
