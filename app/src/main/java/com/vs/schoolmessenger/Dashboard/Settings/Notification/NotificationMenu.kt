package com.vs.schoolmessenger.Dashboard.Settings.Notification

data class NotificationMenu(
    val menu_id: Int,
    val menu_name: String,
    val details: List<NotificationItem>?
)