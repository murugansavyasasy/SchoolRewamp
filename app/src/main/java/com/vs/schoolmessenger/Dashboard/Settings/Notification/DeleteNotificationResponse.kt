package com.vs.schoolmessenger.Dashboard.Settings.Notification

data class DeleteNotificationResponse(
    val status: Boolean,
    val message: String,
    val data: List<Any>
)
