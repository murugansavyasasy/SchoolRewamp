package com.vs.schoolmessenger.Parent.ExamMarks

data class ProgressCardResponse(
    val status: Boolean,
    val message: String,
    val data: List<String>
)
