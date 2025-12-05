package com.vs.schoolmessenger.School.QuizExam.Model.AddQuestion

import com.vs.schoolmessenger.Parent.Assignment.Model.FilePath

data class QuizRequestBody(
    val quiz_id: String,
    val questions: List<QuizQuestionRequest>,       // only API + USER
    val max_mark: Int,                              // sum of all types
    val ok_flag: Boolean,
    val update_question_bank: List<UpdateQBankItem> // only QBANK
)


data class QuizQuestionRequest(
    val ques_no: String,
    val chapter: String,
    val question: String,
    val a_option: String,
    val b_option: String,
    val c_option: String,
    val d_option: String,
    val answer: String,
    val mark: Int,
    val iframe: String? = null,
    val file_size: String? = null,
    val thumbnail: String? = null,
    val file_path: List<FilePath>? = emptyList()
)


data class UpdateQBankItem(
    val ques_no: String,
    val subject_id: String,
    val chapter: String,
    val question: String,
    val a_option: String,
    val b_option: String,
    val c_option: String,
    val d_option: String,
    val answer: String,
    val mark: Int,
//    val iframe: String? = null,
//    val file_size: String? = null,
//    val thumbnail: String? = null,
//    val file_path: List<FilePath> = emptyList()
//    val ques_no: String,
//    val chapter: String,
//    val question: String,
//    val a_option: String,
//    val b_option: String,
//    val c_option: String,
//    val d_option: String,
//    val answer: String,
//    val mark: Int
)

