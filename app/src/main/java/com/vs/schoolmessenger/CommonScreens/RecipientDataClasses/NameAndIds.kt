package com.vs.schoolmessenger.CommonScreens.RecipientDataClasses

import com.google.gson.annotations.SerializedName

data class NameAndIds(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("admission_no") val admission_no: String,
    @SerializedName("roll_no") val roll_no: String,
    @SerializedName("created_on") val created_on: String

)
