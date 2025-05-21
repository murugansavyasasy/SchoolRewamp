package com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Model

data class EventResponse(
    val status: Boolean,
    val message: String,
    val data: List<EventDataClass>
)