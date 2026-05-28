package com.vs.schoolmessenger.Auth.OTP

data class OtpResponse(
    val status: Boolean,
    val message: String,
    val data: List<Any> // Empty list in case of failure
)

