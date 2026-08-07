package com.vs.schoolmessenger.School.ExamReview.Model

data class Meta(
    val generatedAt: String,
    val requestedExamIds: List<String>,
    val maxMarksPerSubject: Int,
    val maxTotal: Int
)
