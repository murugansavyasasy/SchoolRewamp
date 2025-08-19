package com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.RewampModelEvent

data class EventResponse(
    val status: Boolean,
    val message: String,
    val data: List<EventData>
)