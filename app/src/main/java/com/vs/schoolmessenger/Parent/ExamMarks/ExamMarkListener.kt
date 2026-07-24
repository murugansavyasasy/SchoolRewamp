package com.vs.schoolmessenger.Parent.ExamMarks

import com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewampModel.ExamTimetableRubric

interface ExamMarkListener {
    fun onSearchResultEmpty(isEmpty: Boolean)
    fun onExamSelected(examid: String, examName: String,type: String)
    fun onRubricClick(rubric: ExamTimetableRubric)


}
