package com.vs.schoolmessenger.Parent.QuizExam.Model.GetQuestion

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentFile

data class GetQuestionDetails(
    val id: String,
    val question: String,
    val mark: Int,
    val options: List<OptionsData>,
    @SerializedName("q_file_path")
    val file_path: List<AttachmentFile>
)