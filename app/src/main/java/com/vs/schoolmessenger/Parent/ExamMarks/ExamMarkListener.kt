package com.vs.schoolmessenger.Parent.ExamMarks

interface ExamMarkListener {
    fun onSearchResultEmpty(isEmpty: Boolean)
    fun onExamSelected(examid: String, examName: String)

}
