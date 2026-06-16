package com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.ChatModel

data class AnswerData(
    val question_id: String,
    val question: String,
    val student_id: String,
    val student_name: String,
    val asked_on: String,
    val quesfilepath: List<FilePath>,
    val reply_type: String,
    val answer: String,
    val answered_on: String,
    val ansfilepath: List<FilePath>,
    val chat_count: Int,
    val my_question: Boolean
)