package com.vs.schoolmessenger.Auth.OTP

data class ForgetOtpSendResponse(
    val status: Boolean,
    val message: String,
    val data: List<ForgetOtpData>
)