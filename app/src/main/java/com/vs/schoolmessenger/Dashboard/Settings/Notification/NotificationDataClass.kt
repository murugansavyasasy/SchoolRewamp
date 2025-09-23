package com.vs.schoolmessenger.Dashboard.Settings.Notification

data class NotificationDataClass(
    val type: String,
    val title: String? = null,
    val content: String,
    val sendBy: String,
    val isHeader: Boolean = false,
    val category: String? = null,
    val menu_id: Int?,
)
