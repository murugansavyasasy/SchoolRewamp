package com.vs.schoolmessenger.School.InteractionWithStudent.Model

data class StudentChatData(
    val id: String,
    val name: String,
    val section_id: String,
    val section_name: String,
    val subject_id: String,
    val unread_count: Int,
    val subject_name: String,
    val last_msg: String,
    val last_msg_time: String,
    val is_class_teacher: Boolean
)