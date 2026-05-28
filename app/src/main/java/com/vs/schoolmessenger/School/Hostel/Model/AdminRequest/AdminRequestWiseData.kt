package com.vs.schoolmessenger.School.Hostel.Model.AdminRequest

data class AdminRequestWiseData (
    val roomNumber: String,
    val roomTitle: String,
    val studentName: String,
    val issueDescription: String,
    val dateTime: String,
    val status: String
)

