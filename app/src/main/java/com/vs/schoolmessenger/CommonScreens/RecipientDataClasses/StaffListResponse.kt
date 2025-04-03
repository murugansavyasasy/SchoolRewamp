package com.vs.schoolmessenger.CommonScreens.RecipientDataClasses

import com.google.gson.annotations.SerializedName

data class StaffListResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<StaffListData>
)