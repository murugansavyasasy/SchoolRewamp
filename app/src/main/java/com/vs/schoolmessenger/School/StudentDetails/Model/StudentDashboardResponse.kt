package com.vs.schoolmessenger.School.StudentDetails.Model

data class StudentDashboardResponse(
    val status: Boolean,
    val message: String,
    val data: List<StudentDashboardData>
)