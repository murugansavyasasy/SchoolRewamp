package com.vs.schoolmessenger.Parent.RaiseConcern.ParentConcernlistModel

data class ParentConcern (
    val id: String,
    val student_id: String,
    val student_name: String,
    val class_name: String,
    val section_name: String,
    val type_name: String,
    val description: String,
    val status: String,
    val file_path: List<ConcernFile>,
    val acknowledged_by: String,
    val acknowledged_on: String,
    val acknowledgement: String,
    val action_taken: String,
    val action_taken_by: String,
    val action_taken_on: String,
    val raised_on: String
)