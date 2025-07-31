package com.vs.schoolmessenger.School.InteractionWithStudent.Listener

import com.vs.schoolmessenger.School.InteractionWithStudent.Model.QuestionData

interface ReplyClickListener {
    fun onReplyClick(chat: QuestionData, position: Int, replyType: String,type: Boolean)
    fun onReplyAllClick(chat: QuestionData, position: Int, replyType: String,type: Boolean)

}
