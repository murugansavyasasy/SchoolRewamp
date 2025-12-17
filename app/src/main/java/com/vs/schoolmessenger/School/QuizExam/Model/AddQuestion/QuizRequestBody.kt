package com.vs.schoolmessenger.School.QuizExam.Model.AddQuestion

import com.vs.schoolmessenger.Parent.Assignment.Model.FilePath
import com.google.gson.annotations.SerializedName

data class QuizRequestBody(
    val quiz_id: String,
    val questions: List<QuizQuestionRequest>,
    val max_mark: Int,
    val ok_flag: Boolean,
    val update_question_bank: List<UpdateQBankItem>
)

data class QuizQuestionRequest(

    @SerializedName("ques_no")
    val quesNo: String,

    val chapter: String,
    val question: String,

    val a_option: String,
    val b_option: String,
    val c_option: String,
    val d_option: String,

    val answer: String,
    val mark: Int,

    val iframe: String = "",
    val file_size: String = "",
    val thumbnail: String = "",
    @SerializedName("q_file_path")
    var file_path: MutableList<FilePath> = mutableListOf(),

    var a_image: String? = null,
    var b_image: String? = null,
    var c_image: String? = null,
    var d_image: String? = null
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
    val a_image: String,
    val b_image: String,
    val c_image: String,
    val d_image: String,
    val answer: String,
    val mark: Int
)



//data class QuizRequestBody(
//    val quiz_id: String,
//    val questions: List<QuizQuestionRequest>,       // only API + USER
//    val max_mark: Int,                              // sum of all types
//    val ok_flag: Boolean,
//    val update_question_bank: List<UpdateQBankItem> // only QBANK
//)
//
//
//data class QuizQuestionRequest(
//    val ques_no: String,
//    val chapter: String,
//    val question: String,
//    val a_option: String,
//    val b_option: String,
//    val c_option: String,
//    val d_option: String,
//    val answer: String,
//    val mark: Int,
//    val iframe: String? = null,
//    val file_size: String? = null,
//    val thumbnail: String? = null,
//    val file_path: List<FilePath>? = emptyList()
//)
//
//
//data class UpdateQBankItem(
//    val ques_no: String,
//    val subject_id: String,
//    val chapter: String,
//    val question: String,
//    val a_option: String,
//    val b_option: String,
//    val c_option: String,
//    val d_option: String,
//    val answer: String,
//    val mark: Int,
////    val iframe: String? = null,
////    val file_size: String? = null,
////    val thumbnail: String? = null,
////    val file_path: List<FilePath> = emptyList()
////    val ques_no: String,
////    val chapter: String,
////    val question: String,
////    val a_option: String,
////    val b_option: String,
////    val c_option: String,
////    val d_option: String,
////    val answer: String,
////    val mark: Int
//)

