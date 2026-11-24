package com.vs.schoolmessenger.Parent.ExamMarks

import com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewampModel.ExamTimetableRubric

interface ExamMarkListener {
    fun onSearchResultEmpty(isEmpty: Boolean)
    fun onExamSelected(examid: String, examName: String,type: String)
    fun onRubricClick(rubric: ExamTimetableRubric)

    // Called only when mark/report has not been viewed yet. viewType = "mark" or "report"
    fun onExamViewStatus(reportId: Int, viewType: String) {}

}
