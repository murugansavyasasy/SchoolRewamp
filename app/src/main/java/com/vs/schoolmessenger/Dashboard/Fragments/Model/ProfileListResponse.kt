package com.vs.schoolmessenger.Dashboard.Fragments.Model


data class ProfileListResponse(
    val status: Boolean,
    val message: String,
    val data: List<Map<String, List<ProfileField>>>
)
