package com.vs.schoolmessenger.Parent.Assignment.MySubmissionModel

data class MySubmittedAssignmentsResponse(
    val status: Boolean,
    val message: String,
    val data: List<SubmittedAssignment>
)