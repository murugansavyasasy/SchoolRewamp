package com.vs.schoolmessenger.School.QuizExam

import android.widget.EditText
import android.widget.TextView
import com.vs.schoolmessenger.School.QuizExam.Model.QuizQuestionsReport.GetQuizQuestionReportData

interface OnAttachmentListener {
    fun onAttachmentPick(
        position: Int,
        item: MutableList<GetQuizQuestionReportData>?,
        isQuestion: Boolean,
        isOptionsImageId: TextView
    )
}