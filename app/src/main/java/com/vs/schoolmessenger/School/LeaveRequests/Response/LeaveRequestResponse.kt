package com.vs.schoolmessenger.School.LeaveRequests.Response

import com.vs.schoolmessenger.Parent.RequestLeave.MonthWiseLeaveData

data class LeaveRequestResponse(
    val status: Boolean,
    val message: String,
    val data: List<MonthWiseLeaveData>
)



