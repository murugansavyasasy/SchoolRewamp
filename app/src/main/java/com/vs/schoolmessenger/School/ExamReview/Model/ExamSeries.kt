package com.vs.schoolmessenger.School.ExamReview.Model


data class ExamSeries(
    val id: String,
    val title: String,       // e.g. "DRT - 1"
    val seriesLabel: String, // e.g. "Series 1"
    val maxMarks: Int        // e.g. 100
)