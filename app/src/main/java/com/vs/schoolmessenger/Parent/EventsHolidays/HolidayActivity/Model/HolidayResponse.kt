package com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Model

import com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Model.Holiday

data class HolidayResponse(
    val status: Boolean,
    val message: String,
    val data: List<Holiday>
)