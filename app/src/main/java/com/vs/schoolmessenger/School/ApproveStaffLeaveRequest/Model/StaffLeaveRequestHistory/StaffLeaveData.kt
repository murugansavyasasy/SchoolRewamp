package com.vs.schoolmessenger.School.ApproveStaffLeaveRequest.Model.StaffLeaveRequestHistory

data class StaffLeaveData (
    val id: String,
    val staff_id: String,
    val applied_on: String?,
    val staff_name: String?,
    val from_date: String?,
    val email: String?,
    val mobile_no: String?,
    val address: String?,
    val role: String?,
    val to_date: String?,
    val no_of_days: String?,
    val reason: String?,
    var status: String?,
    var priority_level: String?=null,
    val updated_on: String?,
    val from_session: String?,
    val to_session: String?,
    val approved_by: String?,
    val leave_type: String?,
    val leave_type_id: Int?,
    val status_id: Int?,
)

