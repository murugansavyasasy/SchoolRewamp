package com.vs.schoolmessenger.School.InteractionWithStudent.Model

data class QuestionDataSending(
    val id: String,
    val name: String,
    val section_id: String,
    val section_name: String,
    val subject_id: String,
    val subject_name: String,
    val is_class_teacher: Boolean
)