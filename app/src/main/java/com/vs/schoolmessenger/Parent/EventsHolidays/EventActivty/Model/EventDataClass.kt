package com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Model

data class EventDataClass (
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val venue: String,
    val iframe: String,
    val file_size: String,
    val file_path: List<FilePath>
)