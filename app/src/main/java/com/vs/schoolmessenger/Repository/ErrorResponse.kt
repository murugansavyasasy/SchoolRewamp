package com.vs.schoolmessenger.Repository

data class ErrorResponse(
    val status: Boolean,
    val message: String,
    val statusCode: Int
)