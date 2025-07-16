package com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel

data class LeaveRequestDeleteResponse (
    val status: Boolean,
    val message: String,
    val data: List<Any>
)