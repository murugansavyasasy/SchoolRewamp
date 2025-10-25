package com.vs.schoolmessenger.Parent.InteractionWithStaff.Model

data class Staff(
    val id: String,
    val name: String,
    val subject_id: String,
    val subject_name: String,
    val is_assigned: Boolean,
    val is_class_teacher: Boolean,
    val unread_count: String,
    val last_msg_time: String,
    val last_msg: String,
    val is_blocked: Boolean,
    val blocked_on: String,
    val reason: String,
    val section_name: String
)