package com.vs.schoolmessenger.School.PTM.DataClass

data class Slot(val slot_id: String,
                val from_time: String,
                val to_time: String,
                val is_cancelled: Int,
                val is_booked: Int,
                val booked_by: String,
                val my_class: String,
                val my_section: String,
                val profile_url: String,
                val status: String,
                val event_name: String,
                val event_mode: String,
                val meeting_duration: Int,
                val break_duration: Int,
                val is_cancelled_by_staff: Int,
                val date: String)
