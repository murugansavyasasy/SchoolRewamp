package com.vs.schoolmessenger.School.LeaveRequests.Model

data class LeaveData(
    val id: String,
    val applied_on: String,
    val student_name: String,
    val class_name: String,
    val section_name: String,
    val leave_from: String,
    val leave_to: String,
    val no_of_days: String,
    val reason: String,
    val status: String,
    val updated_on: String,
    val from_session: String,
    val to_session: String

)
