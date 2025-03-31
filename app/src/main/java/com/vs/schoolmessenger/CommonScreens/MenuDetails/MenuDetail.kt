package com.vs.schoolmessenger.CommonScreens.MenuDetails

import com.google.gson.annotations.SerializedName

data class MenuDetail(
    val id: Int,
    val name: String,
    @SerializedName("unread_count") val unreadCount: Int
)