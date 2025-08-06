package com.vs.schoolmessenger.Parent.Assignment.Model

import com.vs.schoolmessenger.School.InteractionWithStudent.Model.AnswerModelRequestFilePath

data class AssignmentModelRequest(
    val id: String,
    val description: String,
    val iframe: String,
    val file_size: String,
    val thumbnail: String,
    val file_path: List<AnswerModelRequestFilePath>
)