package com.vs.schoolmessenger.CommonScreens

data class DeviceToken(
    val status: Boolean,
    val message: String,
    val data: List<Any>
) // Empty list in case of success

