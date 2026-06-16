package com.vs.schoolmessenger.Parent.PTM.DataClass

data class MeetingItem(
    val id: String,
    val date: String,
    val time: String,
    val status: String,
    val purpose: String,
    val mode: String,
    val duration: Int,
    val event_link: String,
    val staff_id: String,
    val staff_name: String,
    val is_cancelled_by_staff: Boolean,
    val subject_name: ArrayList<String>,
    val staff_mobile_no: String?
)
