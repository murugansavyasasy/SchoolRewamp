package com.vs.schoolmessenger.School.QuizExam.Model.CreateQuiz

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

data class SaveCreateExamQuizDetails (
    val title: String,
    val description: String,
    val no_of_question: String,
    val level_flag: Boolean,
): Serializable


