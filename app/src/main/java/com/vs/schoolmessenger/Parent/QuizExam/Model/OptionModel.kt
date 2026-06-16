package com.vs.schoolmessenger.Parent.QuizExam.Model


data class OptionModel(
    val option: String,
    val text: String,
    val graphImageUrl: String? = null,
    var isSelected: Boolean = false
)

