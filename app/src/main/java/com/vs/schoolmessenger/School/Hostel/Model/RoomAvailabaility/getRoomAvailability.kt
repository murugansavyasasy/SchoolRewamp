package com.vs.schoolmessenger.School.Hostel.Model.RoomAvailabaility

data class getRoomAvailability (
    val id: String,
    val room_no: String,
    val total_occupancy: String,
    val current_occupancy: String,
    val hostellarDetails: List<getHostellarDetails>
)