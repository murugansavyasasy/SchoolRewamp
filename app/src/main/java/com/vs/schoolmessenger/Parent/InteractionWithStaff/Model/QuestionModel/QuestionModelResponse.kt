package com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.QuestionModel

data class QuestionModelResponse (
    val status: Boolean,
    val message: String,
    val data: List<QuestionDataClass>
)