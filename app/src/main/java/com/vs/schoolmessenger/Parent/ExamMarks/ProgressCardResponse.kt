package com.vs.schoolmessenger.Parent.ExamMarks

data class ProgressCardResponse(
    val status: Boolean,
    val message: String,
    val data: List<ProgressCardData>
)

data class ProgressCardData(
    val link: String?
)