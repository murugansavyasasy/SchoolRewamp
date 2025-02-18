package com.vs.schoolmessenger.School.LeaveRequests

data class LeaveRequestData(
    val isDate: String,
    var isTime: String,
    val isName: String,
    val isSection: String,
    val isLeaveFromDate: String,
    val isLeaveToDate: String,
    val isReason: String,
    val isType: String,
    )
