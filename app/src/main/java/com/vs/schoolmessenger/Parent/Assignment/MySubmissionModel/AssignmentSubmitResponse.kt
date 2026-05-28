package com.vs.schoolmessenger.Parent.Assignment.MySubmissionModel

data class AssignmentSubmitResponse(
    val status: Boolean,
    val message: String,
    val data: List<Any>
)
