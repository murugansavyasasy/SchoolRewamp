package com.vs.schoolmessenger.School.MessageFromManagement.Model

class GetMessagesStaff(
    val status: Boolean,
    val message: String,
    val data: List<GetMessagesStaffData>
)
