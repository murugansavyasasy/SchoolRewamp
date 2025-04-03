package com.vs.schoolmessenger.CommonScreens.MenuDetails

data class DashboardResponse(
       val status: Boolean,
       val message: String,
       val data: List<DashboardData>
)