package com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.QuestionModel.Request

data class QuestionModelRequest(
    val staff_id: String,
    val subject_id: String,
    val question: String,
    val is_class_teacher: Boolean,
    val file_path: List<FilePath>
)