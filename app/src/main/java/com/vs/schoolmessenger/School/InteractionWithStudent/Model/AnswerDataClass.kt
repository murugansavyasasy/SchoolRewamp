package com.vs.schoolmessenger.School.InteractionWithStudent.Model

data class AnswerDataClass(
    val question_id: String,
    val answer: String,
    val answer_on: String,
    val reply_type: String,
    val created_on: String,
    val file_path: List<AnswerDataClassFilePath>
)