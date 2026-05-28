package com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Model

import java.io.Serializable

data class Holiday(
    val name: String,
    val year: String,
    val date: String
) : Serializable