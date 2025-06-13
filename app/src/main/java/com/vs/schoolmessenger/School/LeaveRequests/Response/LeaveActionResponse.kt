package com.vs.schoolmessenger.School.LeaveRequests.Response

data class LeaveActionResponse(
    val status: Boolean,
    val message: String,
    val data: List<Any>
)
