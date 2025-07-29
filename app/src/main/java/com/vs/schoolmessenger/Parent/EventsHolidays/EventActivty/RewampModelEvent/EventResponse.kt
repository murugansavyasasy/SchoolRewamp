package com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.RewampModelEvent

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Model.EventDataClass
import com.vs.schoolmessenger.Repository.APIKeyNames

data class EventResponse(
    val status: Boolean,
    val message: String,
    val data: List<EventData>
)