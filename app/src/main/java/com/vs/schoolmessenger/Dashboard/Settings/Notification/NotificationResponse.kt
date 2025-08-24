package com.vs.schoolmessenger.Dashboard.Settings.Notification

data class NotificationResponse(
    val status: Boolean,
    val message: String,
    val data: List<NotificationMenu>
)