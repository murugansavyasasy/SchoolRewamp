package com.vs.schoolmessenger.School.Hostel.Model.HostelDashboard.RoomAvailabaility

data class isSelectedRoomData (
    val id: String,
    val number: String,
    val current_occupancy: Int,
    val max_occupancy: Int,
    val total_beds: Int,
    val isSelectedAcademicYear: Int?,

    )