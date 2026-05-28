package com.vs.schoolmessenger.Parent.QuizExam.Model


data class QuizQuestion(
    val questionText: String,
    val attachments: List<String>,
    val options: List<OptionModel>
)