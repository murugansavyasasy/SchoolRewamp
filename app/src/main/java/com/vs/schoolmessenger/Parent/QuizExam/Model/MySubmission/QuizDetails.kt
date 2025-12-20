package com.vs.schoolmessenger.Parent.QuizExam.Model.MySubmission

import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentFile
import com.vs.schoolmessenger.Parent.QuizExam.Model.GetQuestion.OptionsData

class QuizDetails(
    val id: String,
    val quiz_id: String,
    val question: String,
    val options: List<OptionsData>,
    val mark: Int,
    val student_answer: String,
    val correct_answer: String,
    val q_file_path: List<AttachmentFile>
)
