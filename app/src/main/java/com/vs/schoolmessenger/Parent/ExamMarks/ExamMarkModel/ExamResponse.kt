package com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkModel

import com.vs.schoolmessenger.Parent.ExamMarks.Model.ExamData

data class ExamResponse(
    val status: Boolean,
    val message: String,
    val data: List<ExamData>
)


