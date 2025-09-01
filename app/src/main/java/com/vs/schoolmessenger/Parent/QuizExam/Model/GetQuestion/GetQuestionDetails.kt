package com.vs.schoolmessenger.Parent.QuizExam.Model.GetQuestion

import com.vs.schoolmessenger.Parent.Assignment.Model.FilePath

class GetQuestionDetails(
    val id: String,
    val question: String,
    val mark: Int,
    val options: List<String>,
    val file_path:List<FilePath>
)