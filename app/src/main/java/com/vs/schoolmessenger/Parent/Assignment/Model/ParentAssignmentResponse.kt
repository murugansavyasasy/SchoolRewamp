package com.vs.schoolmessenger.Parent.Assignment.Model

data class ParentAssignmentResponse(
    val status: Boolean,
    val message: String,
    val data: List<ParentAssignmentData>
)