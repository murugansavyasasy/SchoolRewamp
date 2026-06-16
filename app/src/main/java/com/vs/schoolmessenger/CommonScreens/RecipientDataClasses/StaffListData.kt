package com.vs.schoolmessenger.CommonScreens.RecipientDataClasses

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

data class StaffListData(
    @SerializedName(APIKeyNames.id) val id: Int,
    @SerializedName(APIKeyNames.name) val name: String
)