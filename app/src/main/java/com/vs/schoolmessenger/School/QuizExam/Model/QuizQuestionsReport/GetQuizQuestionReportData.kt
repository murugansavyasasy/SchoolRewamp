package com.vs.schoolmessenger.School.QuizExam.Model.QuizQuestionsReport

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Parent.Assignment.Model.FilePath

enum class QuestionSource {
    USER, API, QBANK
}

data class GetQuizQuestionReportData(

    val id: String,
    val quiz_id: String? = null,
    var question: String,
    var chapter: String,
    var answer: String,
    var a_option: String,
    var b_option: String,
    var c_option: String,
    var d_option: String,
    var mark: Int,
    val iframe: String? = "",
    val file_size: String? = "",
    val thumbnail: String? = "",
    var a_image: String? = "",
    var b_image: String? = "",
    var c_image: String? = "",
    var d_image: String? = "",
    var sourceType: QuestionSource = QuestionSource.API,
    @SerializedName("q_file_path")
    var file_path: MutableList<FilePath>? = mutableListOf()
)
