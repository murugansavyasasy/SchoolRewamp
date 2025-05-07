package com.vs.schoolmessenger.CommonScreens.MenuDetails

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

data class MenuDetail(
    val id: Int,
    val name: String,
    @SerializedName(APIKeyNames.unread_count) val unreadCount: Int
)