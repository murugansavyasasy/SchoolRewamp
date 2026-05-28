package com.vs.schoolmessenger.Parent.Assignment.Model

data class AssignmentSubmitResponse(
    val status: Boolean,
    val message: String,
    val data: List<Any>
)
