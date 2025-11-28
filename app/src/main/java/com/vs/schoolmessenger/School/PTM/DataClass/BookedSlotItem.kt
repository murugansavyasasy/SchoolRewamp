package com.vs.schoolmessenger.School.PTM.DataClass

data class BookedSlotItem(
    val slot_id: String?,
    val date: String?,
    val from_time: String?,
    val to_time: String?,
    val event_name: String?,
    val event_mode: String?,
    val meeting_duration: Int?,
    val event_link: String?,
    val sent_by: String?,
    val student_id: String?,
    val student_name: String?,
    val father_name: String?,
    val mother_name: String?,
    val mobile_no: String?,
    val class_id: String?,
    val section_id: String?,
    val class_name: String?,
    val section_name: String?,
    val slot_status: String?,
    val is_booked: Boolean?,
    val profile_url: String?,
    val can_cancel: Boolean?,
    val is_cancelled: Boolean?,
    val is_cancelled_by_staff: Boolean?
)