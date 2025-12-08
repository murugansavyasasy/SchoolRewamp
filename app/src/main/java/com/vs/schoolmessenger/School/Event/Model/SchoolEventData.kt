package com.vs.schoolmessenger.School.Event.Model

data class SchoolEventData(
    val categories: List<EventCategory>,
    val on_going: List<SchoolEventItem>,
    val up_coming: List<SchoolEventItem>,
    val completed: List<SchoolEventItem>
)