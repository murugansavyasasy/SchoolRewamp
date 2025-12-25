package com.vs.schoolmessenger.School.ExamMarkUpload.UploadMarkSheet.Model

import com.google.gson.annotations.SerializedName

data class ReviewFlag(
    @SerializedName("student_id")
    val studentId: String,
    val field: String,
    val value: String,
    val reason: String
)