package com.vs.schoolmessenger.School.Event.Model

import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.RewampModelEvent.EventData

data class SchoolEventResponse (
    val status: Boolean,
    val message: String,
    val data: List<SchoolEventData>
)