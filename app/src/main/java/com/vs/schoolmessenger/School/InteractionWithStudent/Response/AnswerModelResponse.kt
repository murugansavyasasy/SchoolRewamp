package com.vs.schoolmessenger.School.InteractionWithStudent.Response

import com.vs.schoolmessenger.School.InteractionWithStudent.Model.AnswerDataClass

data class AnswerModelResponse(
    val status: Boolean,
    val message: String,
    val data: List<AnswerDataClass>
)