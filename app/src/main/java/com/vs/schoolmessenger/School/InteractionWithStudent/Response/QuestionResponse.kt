package com.vs.schoolmessenger.School.InteractionWithStudent.Response

import com.vs.schoolmessenger.School.InteractionWithStudent.Model.QuestionData

data class QuestionResponse(
    val status: Boolean,
    val message: String,
    val data: List<QuestionData>
)