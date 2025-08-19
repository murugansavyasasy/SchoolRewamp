package com.vs.schoolmessenger.School.LSRW.Model

data class lsrwskilldata(
    val id: String,
    val title: String,
    val description: String,
    val sent_to: String,
    val activity_type: String,
    val created_on: String,
    val file_path: List<lsrwfilepath>,
    val iframe: String,
    val file_size: String,
    val thumbnail: String
)