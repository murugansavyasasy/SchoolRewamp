package com.vs.schoolmessenger.School.InteractionWithStudent.Model

data class BlockedStudent(
    val id: String,
    val name: String,
    val class_id: String,
    val class_name: String,
    val section_id: String,
    val section_name: String,
    val gender: String,
    val reason: String,
    val blocked_on: String
)