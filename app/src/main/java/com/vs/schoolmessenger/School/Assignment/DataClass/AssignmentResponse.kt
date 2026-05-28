package com.vs.schoolmessenger.School.Assignment.DataClass

data class AssignmentResponse(
    val status: Boolean,
    val message: String,
    val data: List<AssignmentData>
)
