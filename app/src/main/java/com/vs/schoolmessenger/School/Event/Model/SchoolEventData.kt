package com.vs.schoolmessenger.School.Event.Model

import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.RewampModelEvent.Category
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.RewampModelEvent.EventItem

data class SchoolEventData (
    val on_going: List<SchoolEventItem>,
    val up_coming: List<SchoolEventItem>,
    val completed: List<SchoolEventItem>
)