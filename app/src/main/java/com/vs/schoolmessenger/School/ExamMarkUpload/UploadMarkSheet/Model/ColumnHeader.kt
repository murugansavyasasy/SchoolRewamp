package com.vs.schoolmessenger.School.ExamMarkUpload.UploadMarkSheet.Model

import com.google.gson.annotations.SerializedName

data class ColumnHeader(
    val position: Int,
    val name: String,
    val type: String,
    val subject: String?,
    val assessment: String?,
    @SerializedName("max_marks")
    val maxMarks: Int,
    val confidence: String
)