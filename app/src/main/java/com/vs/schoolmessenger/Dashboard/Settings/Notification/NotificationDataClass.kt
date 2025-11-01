package com.vs.schoolmessenger.Dashboard.Settings.Notification

data class NotificationDataClass(
    val id: String,
    val type: String,
    val title: String? = null,
    val sent_on: String,
    val content: String,
    val sendBy: String,
    val name: String,
    val isHeader: Boolean = false,
    val category: String? = null,
    val menu_id: Int?,
)
