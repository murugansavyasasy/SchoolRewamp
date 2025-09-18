package com.vs.schoolmessenger.School.PTM.DataClass

import java.io.Serializable

data class Slot(
    val slot_id: String,
    val from_time: String,
    val to_time: String,
    val sent_by: String,
    val can_cancel: Boolean,
    val is_cancelled: Boolean,
    val is_booked: Boolean,
    val booked_by: String,
    val my_class: String,
    val my_section: String,
    val profile_url: String,
    val mobile_no: String,
    val status: String,
    val event_name: String,
    val event_mode: String,
    val meeting_duration: Int,
    val break_duration: String,
    val is_cancelled_by_staff: Boolean,
    val date: String
) : Serializable
