package com.vs.schoolmessenger.Parent.Noticeboard

data class Notice(
    val title: String,
    val id: String,
    val description: String,
    val created_on: String,
    val sent_by: String,
    val day: String,
    val visible_from: String,
    val visible_to: String,
    val intended_for: String,
    val is_management: Boolean,
    val iframe: String,
    val file_path: List<FilePath>
)