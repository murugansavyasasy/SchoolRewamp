package com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.RewampModelEvent

data class EventItem(
    val title: String,
    val category: String,
    val description: String,
    val date: String,
    val time: String,
    val venue: String,
    val iframe: String,
    val file_size: String,
    val thumbnail: String,
    val file_path: List<FilePath>
)