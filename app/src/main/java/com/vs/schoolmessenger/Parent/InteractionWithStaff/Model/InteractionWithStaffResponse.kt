package com.vs.schoolmessenger.Parent.InteractionWithStaff.Model

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

data class InteractionWithStaffResponse(
    @SerializedName(APIKeyNames.status)
    val status: Boolean,
    @SerializedName(APIKeyNames.message)
    val message: String,
    @SerializedName(APIKeyNames.data)
    val data: List<Staff>
)


