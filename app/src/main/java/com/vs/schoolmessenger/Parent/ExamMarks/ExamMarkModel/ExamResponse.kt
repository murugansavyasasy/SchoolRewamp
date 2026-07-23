package com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkModel


data class ExamResponse(
    val status: Boolean,
    val message: String,
    val data: List<ExamDataRewamp>
)


