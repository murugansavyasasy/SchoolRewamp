package com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkResultsModel

data class ExamMarksResponse (
    val status: Boolean,
    val message: String,
    val data: List<ExamMarkData>
)


