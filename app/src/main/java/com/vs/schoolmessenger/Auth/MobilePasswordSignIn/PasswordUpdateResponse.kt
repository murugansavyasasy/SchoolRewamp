package com.vs.schoolmessenger.Auth.MobilePasswordSignIn

data class PasswordUpdateResponse(
    val status: Boolean,
    val message: String,
    val data: List<PasswordUpdateData>
)
