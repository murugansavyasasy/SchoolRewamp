package com.vs.schoolmessenger.School.Hostel.Model.HostelDashboard.RoomAvailabaility

data class getRoomAvailability (
    val id: String,
    val number: String,
    val current_occupancy: Int,
    val max_occupancy: Int,
    val total_beds: Int,
    val students: List<String>
)