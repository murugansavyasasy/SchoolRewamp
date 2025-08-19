package com.vs.schoolmessenger.Parent.QuizExam.Model.GetQuestion

import com.vs.schoolmessenger.Parent.QuizExam.Model.QuizExamList.GetQuizExamListData

class GetQuizQuestions(
    val status: Boolean,
    val message: String,
    val data: List<GetQuizQuestionsData>
)