package com.vs.schoolmessenger.School.Hostel.Model.HostelDashboard

import com.vs.schoolmessenger.School.Hostel.Model.HostelDashboard.RoomAvailabaility.getFloorwiseAvailability

data class getHostelDashboardData (
    val stats: getHotelStats,
    val floors: List<getFloorwiseAvailability>
)