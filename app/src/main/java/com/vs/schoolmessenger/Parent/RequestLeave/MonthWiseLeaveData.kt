package com.vs.schoolmessenger.Parent.RequestLeave

import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveData

data class MonthWiseLeaveData(
    val header_id: String,
    val month: String,
    val details: List<LeaveData>
)