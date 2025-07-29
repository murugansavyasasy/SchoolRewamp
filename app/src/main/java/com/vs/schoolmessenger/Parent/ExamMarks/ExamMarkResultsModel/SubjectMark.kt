package com.vs.schoolmessenger.Parent.ExamMarks.ExamMarkResultsModel

data class SubjectMark(
    val name: String,
    val split: List<SplitMark>,
    val max_mark: String,
    val mark_obtained: String,
    val percentage: String

)
