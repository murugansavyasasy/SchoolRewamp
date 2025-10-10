package com.vs.schoolmessenger.School.Event.Model

import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.RewampModelEvent.Category

data class SchoolEventData(
    val categories: List<EventCategory>,
    val on_going: List<SchoolEventItem>,
    val up_coming: List<SchoolEventItem>,
    val completed: List<SchoolEventItem>
)