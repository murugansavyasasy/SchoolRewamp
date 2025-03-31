package com.vs.schoolmessenger.Dashboard.School

data class DashboardResponse(  val status: Boolean,
                               val message: String,
                               val data: List<DashboardData>)
