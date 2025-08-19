package com.vs.schoolmessenger.School.InteractionWithStudent.Model

data class AnswerModelRequest(
    val question_id: String,
    val answer: String,
    val reply_type: String,
    val is_change_answer: Boolean,
    val file_path: List<AnswerModelRequestFilePath>
)