package com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.Model.StaffLeaveRequestHistory


data class StaffMonthWiseLeaveData (
    val month: String,
    val details: List<StaffLeaveData>
)