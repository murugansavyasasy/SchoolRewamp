package com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.RewampModelEvent

data class EventData(
    val categories: List<Category>,
    val on_going: List<EventItem>,
    val up_coming: List<EventItem>,
    val completed: List<EventItem>
)