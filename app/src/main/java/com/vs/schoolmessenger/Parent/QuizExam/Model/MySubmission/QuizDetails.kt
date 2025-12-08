package com.vs.schoolmessenger.Parent.QuizExam.Model.MySubmission

import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentFile

class QuizDetails(
    val id: String,
    val quiz_id: String,
    val question: String,
    val a_option: String,
    val b_option: String,
    val c_cption: String,
    val d_option: String,
    val mark: Int,
    val student_answer: String,
    val correct_answer: String,
    val file_path: List<AttachmentFile>
)
