package com.vs.schoolmessenger.School.Event.Model

data class SchoolEventResponse(
    val status: Boolean,
    val message: String,
    val data: List<SchoolEventData>
)