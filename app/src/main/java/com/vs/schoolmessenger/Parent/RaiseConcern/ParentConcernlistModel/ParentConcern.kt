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
    val can_delete: Boolean,
    val file_path: List<ConcernFile>,
    val acknowledged_by: String,
    val acknowledged_on: String,
    val acknowledgement: String,
    val action_taken: String,
    val action_taken_by: String,
    val action_taken_on: String,
    val action_file_path: List<ConcernFile>,
    val raised_on: String,
    val is_acknowledged: Boolean,
    val is_action: Boolean
)