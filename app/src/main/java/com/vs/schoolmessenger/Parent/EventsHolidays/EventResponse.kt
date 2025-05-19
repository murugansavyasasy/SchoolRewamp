package com.vs.schoolmessenger.Parent.EventsHolidays

data class EventResponse(
    val status: Boolean,
    val message: String,
    val data: List<Event>
)