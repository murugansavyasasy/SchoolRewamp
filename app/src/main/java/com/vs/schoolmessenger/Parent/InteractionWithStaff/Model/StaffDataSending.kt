package com.vs.schoolmessenger.Parent.InteractionWithStaff.Model

data class StaffDataSending(
    var id: String,
    var name: String,
    var subject_id: String,
    var subject_name: String,
    val is_assigned: Boolean,
    val is_class_teacher: Boolean,
    val unread_count: String
)