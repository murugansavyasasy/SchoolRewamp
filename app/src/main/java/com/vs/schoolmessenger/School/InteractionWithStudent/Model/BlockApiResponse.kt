package com.vs.schoolmessenger.School.InteractionWithStudent.Model

data class BlockApiResponse (
    val status: Boolean,
    val message: String,
    val data: List<Any>?
)