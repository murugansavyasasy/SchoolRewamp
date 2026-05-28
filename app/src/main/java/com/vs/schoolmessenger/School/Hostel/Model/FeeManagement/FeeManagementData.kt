package com.vs.schoolmessenger.School.Hostel.Model.FeeManagement

data class FeeManagementData (
    val studentName: String,
    val roomDetails: String,
    val year: String,
    val amount: Double,
    val dueDate: String,
    val status: String
)