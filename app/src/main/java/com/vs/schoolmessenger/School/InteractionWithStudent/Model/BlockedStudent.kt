package com.vs.schoolmessenger.School.InteractionWithStudent.Model

data class BlockedStudent(
    val id: String,
    val name: String,
    val gender: String,
    val reason: String,
    val class_name: String,
    val section_name: String,
    val blocked_on: String
)