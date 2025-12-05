package com.vs.schoolmessenger.Parent.QuizExam.Model.MySubmission

class GetMySubmissionData(
    val student_id: String,
    val right_answer: String,
    val wrong_answer: String,
    val un_answer: String,
    val message: String,
    val quiz_details: List<QuizDetails>
)
