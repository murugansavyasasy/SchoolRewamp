package com.vs.schoolmessenger.School.Hostel.Model.HostelDashboard.RoomAvailabaility


data class getFloorwiseAvailability (
    val id: String,
    val floor_no: String,
    val floor_name: String,
    val rooms: List<getRoomAvailability>
)