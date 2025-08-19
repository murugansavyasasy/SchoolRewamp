package com.vs.schoolmessenger.Parent.RequestLeave

import com.vs.schoolmessenger.School.LeaveRequests.Model.LeaveData

class MonthWiseLeaveData(
    val month: String,
    val details: List<LeaveData>
)