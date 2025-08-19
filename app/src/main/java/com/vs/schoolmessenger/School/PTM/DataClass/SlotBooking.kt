package com.vs.schoolmessenger.School.PTM.DataClass

data class SlotBooking(
    val id: String,
    val event_date: String,
    val slot_from: String,
    val slot_to: String,
    val event_name: String,
    val event_mode: String,
    val event_link: String,
    val student_id: String,
    val student_name: String,
    val class_id: String,
    val section_id: String,
    val class_name: String,
    val section_name: String,
    val slot_status: String,
    val is_booked: Int,
    val profile_url: String
)
