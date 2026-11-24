package com.vs.schoolmessenger.Parent.ExamMarks.ReadStatusModel

data class ViewStatusResponse(
    val status: Boolean,
    val message: String,
    val data: List<ViewStatusData>
)