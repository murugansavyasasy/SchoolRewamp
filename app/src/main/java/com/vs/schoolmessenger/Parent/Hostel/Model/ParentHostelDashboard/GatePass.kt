package com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard

data class GatePass(
    val action_by: String,
    val admission_no: String,
    val reason: String,
    val profile: String,
    val floor_no: String,
    val room_no: String,
    val fromdate_todate: String,
    val request_time: String,
    val status: String
)