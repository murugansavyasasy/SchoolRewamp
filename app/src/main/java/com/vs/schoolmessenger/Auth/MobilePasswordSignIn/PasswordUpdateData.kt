package com.vs.schoolmessenger.Auth.MobilePasswordSignIn

data class PasswordUpdateData(
    val is_number_exists: Boolean,
    val is_password_updated: Boolean,
    val otp_sent: Boolean,
    val otp: String,
    val message: String,
    val more_info: String,
    val dial_numbers: String
)
