package com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel

data class LeaveUpdateResponse(
    val status: Boolean,
    val message: String,
    val data: List<Any>
)
