package com.vs.schoolmessenger.School.QuizExam.Model.QuizSubmissionList

import com.vs.schoolmessenger.School.QuizExam.Model.QuizReport.GetQuizExamReportData

class GetQuizSubmissionList (
    val status: Boolean,
    val message: String,
    val data: List<GetQuizSubmissionListData>
)