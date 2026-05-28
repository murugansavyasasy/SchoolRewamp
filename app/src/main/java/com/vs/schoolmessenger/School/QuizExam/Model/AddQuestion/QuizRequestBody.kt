package com.vs.schoolmessenger.School.QuizExam.Model.AddQuestion

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Parent.Assignment.Model.FilePath
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuizRequestBody(
    val quiz_id: String,
    val questions: List<QuizQuestionRequest>,
    val max_mark: Int,
    val ok_flag: Boolean,
    val open_to_student: Boolean,
    val update_question_bank: List<UpdateQBankItem>
) : Parcelable


@Parcelize
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

) : Parcelable


@Parcelize
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
) : Parcelable
