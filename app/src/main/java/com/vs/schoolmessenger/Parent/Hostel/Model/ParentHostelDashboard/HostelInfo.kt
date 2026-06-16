package com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard

data class HostelInfo(
    val hostel_id: String,
    val hostel_name: String,
    val hostel_type: String,
    val no_of_floors: Int,
    val no_of_rooms: Int,
    val warden_type: String,
    val max_capacity: Int,
    val warden_name: List<String>,
    val institute_name: String,
    val institute_address: String
)