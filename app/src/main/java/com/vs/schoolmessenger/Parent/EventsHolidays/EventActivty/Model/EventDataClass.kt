package com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Model

import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Model.FilePath

data class EventDataClass (
    val title: String,
    val content: String,
    val date: String,
    val time: String,
    val venue: String,
    val file_path: List<FilePath>
)