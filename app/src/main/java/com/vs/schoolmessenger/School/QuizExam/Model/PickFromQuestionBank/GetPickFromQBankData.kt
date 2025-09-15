package com.vs.schoolmessenger.School.QuizExam.Model.PickFromQuestionBank

 data class GetPickFromQBankData (
    val id: String,
    val topic: String,
    val chapter: String,
    val class_id: String,
    val section_id: String,
    val subject_id: String,
    val level: String?,
    val question: String,
    val answer: String,
    val a_option: String,
    val b_option: String,
    val c_option: String,
    val d_option: String,
    val mark: Int,
    val correct_answer_text: String,
    var checked: Boolean = false//This we added to handle the logic
)
