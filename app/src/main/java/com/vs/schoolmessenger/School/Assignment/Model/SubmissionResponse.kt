package com.vs.schoolmessenger.School.Assignment.Model

data class SubmissionResponse (
    val status: Boolean,
    val message: String,
    val data: List<StudentSubmission>
)