package com.vs.schoolmessenger.School.QuizExam.Model.QuizQuestionsReport

import com.vs.schoolmessenger.Parent.Assignment.Model.FilePath

enum class QuestionSource {
    USER, API, QBANK
}

data class GetQuizQuestionReportData (
    val id: String,
    val quiz_id: String,
    var question: String,
    var chapter: String,
    var answer: String,
    var a_option: String,
    var b_option: String,
    var c_option: String,
    var d_option: String,
    var mark: Int,
//    val option_a_counts: Int?=null,
//    val option_b_counts: Int?=null,
//    val option_c_counts: Int?=null,
//    val option_d_counts: Int?=null,
//    val correct_answer_counts: Int?=null,
//    val incorrect_answer_counts: Int?=null,
//    val correct_answer_text: String,
    val iframe: String? = null,
    val file_size: String? = null,
    val thumbnail: String? = null,
    var sourceType: QuestionSource = QuestionSource.API,
    val file_path: List<FilePath> = emptyList()

)