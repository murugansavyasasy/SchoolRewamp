package com.vs.schoolmessenger.School.QuizExam

import com.vs.schoolmessenger.School.QuizExam.Model.QuizQuestionsReport.GetQuizQuestionReportData

interface AddQuestionListner {
    fun onCountUpdated()
    fun onUICheck(list: List<GetQuizQuestionReportData>)

    fun onDeleteQuizQuestion(
        id: String,
        onResult: (Boolean) -> Unit
    )
}
