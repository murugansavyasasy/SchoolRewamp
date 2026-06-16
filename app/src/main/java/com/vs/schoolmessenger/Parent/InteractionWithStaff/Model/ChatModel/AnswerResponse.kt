package com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.ChatModel

data class AnswerResponse(
    val status: Boolean,
    val message: String,
    val data: List<AnswerData>
)