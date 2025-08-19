package com.vs.schoolmessenger.Parent.QuizExam.Model.MySubmission

import com.vs.schoolmessenger.Parent.QuizExam.Model.QuizExamList.GetQuizExamListData

class GetMySubmission (
    val status: Boolean,
    val message: String,
    val data: List<GetMySubmissionData>
)