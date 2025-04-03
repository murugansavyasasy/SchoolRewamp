package com.vs.schoolmessenger.CommonScreens.RecipientDataClasses

import com.google.gson.annotations.SerializedName

data class StaffListData(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)