package com.vs.schoolmessenger.CommonScreens.MenuDetails

data class DashboardCountResponse(
    val status: Boolean,
    val message: String,
    val data: List<DashboardCountData>
)