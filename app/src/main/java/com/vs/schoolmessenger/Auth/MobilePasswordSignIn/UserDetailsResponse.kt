package com.vs.schoolmessenger.Auth.MobilePasswordSignIn

data class UserDetailsResponse(
    val status: Boolean,
    val message: String,
    val data: List<UserDetails>
)
