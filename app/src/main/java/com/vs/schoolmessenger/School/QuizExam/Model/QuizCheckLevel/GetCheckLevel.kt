package com.vs.schoolmessenger.School.QuizExam.Model.QuizCheckLevel

import com.vs.schoolmessenger.School.QuizExam.Model.QuizReport.GetQuizExamReportData

class GetCheckLevel (
    val status: Boolean,
    val message: String,
    val data: List<GetCheckLevelData>
)
