package com.vs.schoolmessenger.School.QuizExam.Model.QuizReport

data class GetQuizExamReportData(
    val id: String,
    val sent_time: String,
    val title: String,
    val description: String,
    val standard: String,
    val section: String,
    val level: Int,
    val subject: String,
    val sent_by: String,
    val submission_date: String,
    val mark: String,
    val type_name: String,
    val submitted_count: Int,
    val no_of_questions: Int,
    val subject_id: String,
)