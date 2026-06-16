package com.vs.schoolmessenger.Auth.CreateResetChangePassword

data class PasswordCreationResponse(
    val status: Boolean,
    val message: String,
    val data: List<Any> // Empty list in case of success
)
