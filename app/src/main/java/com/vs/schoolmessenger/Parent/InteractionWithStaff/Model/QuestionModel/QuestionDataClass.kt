package com.vs.schoolmessenger.Parent.InteractionWithStaff.Model.QuestionModel

data class QuestionDataClass(
    val id: String,
    val name: String,
    val section_id: String,
    val question: String,
    val created_on: String,
    val chat_count: Int,
    val file_path: List<FilePath>
)