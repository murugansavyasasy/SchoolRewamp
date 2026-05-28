package com.vs.schoolmessenger.School.QuizExam

import com.vs.schoolmessenger.School.QuizExam.Model.QuizReport.GetQuizExamReportData

interface ExamQuizReportListener {
    fun onEditClick(data: GetQuizExamReportData, position: Int)
    fun onDeleteClick(id: String, position: Int)
}