package com.vs.schoolmessenger.Parent.QuizExam.Model.GetQuestion

import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentFile

data class QuestionData(
    val id: String,
    val question: String,
    val option1: String,
    val option2: String,
    val option3: String,
    val option4: String,
    val filePath: List<AttachmentFile>
)
