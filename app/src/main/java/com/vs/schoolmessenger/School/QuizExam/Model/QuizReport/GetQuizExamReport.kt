package com.vs.schoolmessenger.School.QuizExam.Model.QuizReport

import com.vs.schoolmessenger.Parent.QuizExam.Model.GetQuestion.GetQuizQuestionsData

class GetQuizExamReport (
    val status: Boolean,
    val message: String,
    val data: List<GetQuizExamReportData>
)