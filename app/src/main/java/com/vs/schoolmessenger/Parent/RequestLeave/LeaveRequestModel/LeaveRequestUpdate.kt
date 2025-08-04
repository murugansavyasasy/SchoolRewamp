package com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequestModel

data class LeaveRequestUpdate (
    val id: String,
    val leave_from: String,
    val leave_to: String,
    val reason: String,
    val f_session: String,
    val t_session: String,
)