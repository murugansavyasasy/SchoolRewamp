package com.vs.schoolmessenger.School.QuizExam.Model.QuizQuestionsReport

import com.vs.schoolmessenger.School.QuizExam.Model.QuizCheckLevel.GetCheckLevelData

class GetQuizQuestionReport (
    val status: Boolean,
    val message: String,
    val data: List<GetQuizQuestionReportData>
)