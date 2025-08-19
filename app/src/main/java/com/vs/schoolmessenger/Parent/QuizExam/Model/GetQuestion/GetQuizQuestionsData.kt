package com.vs.schoolmessenger.Parent.QuizExam.Model.GetQuestion

class GetQuizQuestionsData (
    val level: Int,
    val total_questions: Int,
    val question_details: List<GetQuestionDetails>
)
