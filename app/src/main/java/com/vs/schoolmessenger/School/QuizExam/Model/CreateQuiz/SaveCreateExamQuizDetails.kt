package com.vs.schoolmessenger.School.QuizExam.Model.CreateQuiz


import java.io.Serializable

data class SaveCreateExamQuizDetails(
    val title: String,
    val description: String,
    val no_of_question: String,
    val level_flag: Boolean,
    val type: String?,// this is not coming from api we defined it handle in api,We just put this to handle in Recipient Screen
) : Serializable


