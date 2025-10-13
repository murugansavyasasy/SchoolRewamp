package com.vs.schoolmessenger.School.Assignment.AssignmentTargetDetails

data class AssignmentTargetDetailsResponse (
    val status: Boolean,
    val message: String,
    val data: List<AssignmentTargetDetail>
)