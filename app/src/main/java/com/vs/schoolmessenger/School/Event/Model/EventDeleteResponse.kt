package com.vs.schoolmessenger.School.Event.Model

data class EventDeleteResponse (
    val status: Boolean,
    val message: String,
    val data: List<Any>
)