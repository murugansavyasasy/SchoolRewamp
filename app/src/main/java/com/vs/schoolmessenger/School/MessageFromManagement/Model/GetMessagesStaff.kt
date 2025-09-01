package com.vs.schoolmessenger.School.MessageFromManagement.Model

import com.vs.schoolmessenger.School.QuizExam.Model.QuizReport.GetQuizExamReportData

class GetMessagesStaff (
    val status: Boolean,
    val message: String,
    val data: List<GetMessagesStaffData>
)
