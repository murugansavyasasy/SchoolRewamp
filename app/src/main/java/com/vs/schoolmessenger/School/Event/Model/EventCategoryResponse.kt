package com.vs.schoolmessenger.School.Event.Model

data class EventCategoryResponse( val status: Boolean,
                                  val message: String,
                                  val data: List<EventCategory>)
