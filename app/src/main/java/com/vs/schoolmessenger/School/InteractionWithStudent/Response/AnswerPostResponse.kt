package com.vs.schoolmessenger.School.InteractionWithStudent.Response

import com.vs.schoolmessenger.School.InteractionWithStudent.Model.AnswerData

data class AnswerPostResponse(
    val status: Boolean,
    val message: String,
    val data: List<AnswerData>
)
