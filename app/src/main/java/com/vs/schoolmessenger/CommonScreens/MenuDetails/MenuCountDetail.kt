package com.vs.schoolmessenger.CommonScreens.MenuDetails

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

class MenuCountDetail(
    val id: Int,
    val name: String,
    @SerializedName(APIKeyNames.unread_count) val unread_count: Int
)