package com.vs.schoolmessenger.Auth.Splash

data class VersionCheckResponse(
    val status: Boolean,
    val message: String,
    val data: List<VersionData>
)