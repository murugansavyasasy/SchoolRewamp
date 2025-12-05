package com.vs.schoolmessenger.School.InteractionWithStudent.Listener

import com.vs.schoolmessenger.School.InteractionWithStudent.Model.QuestionData

interface ReplyClickListener {
    fun onAnswerClick(chat: QuestionData, position: Int)
    fun onUpdateAnswerClick(chat: QuestionData, position: Int, type: Boolean)
    fun onBlockStudent(chat: QuestionData, reason: String)

}
