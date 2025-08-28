package com.vs.schoolmessenger.Dashboard.Settings.Notification

data class NotificationItem(
    val id: String,
    val name: String,
    val member_id: String,
    val type: String,
    val menu_id: Int,
    val message: String,
    val sent_on: String,
    val header_id: String,
    val device: String
)
