package com.vs.schoolmessenger.School.InteractionWithStudent.Model

data class QuestionData(
    val id: String,
    val question: String,
    val student_id: String,
    val student_name: String,
    val created_on: String,
    val chat_count: Int,
    val ques_file_path: List<QuesFilePath>,
    val answer: String,
    val is_blocked: Boolean,
    val answer_on: String,
    val change_answer: String,
    val ans_file_path: List<Any>,
    val reply_type: String
)