package com.vs.schoolmessenger.School.QuizExam

import com.vs.schoolmessenger.School.QuizExam.Model.QuizQuestionsReport.GetQuizQuestionReportData

interface OnAttachmentListener {
    fun onAttachmentPick(position: Int, item: MutableList<GetQuizQuestionReportData>?)
}