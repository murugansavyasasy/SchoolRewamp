package com.vs.schoolmessenger.School.ClassTest.UploadMarks.Model

import com.google.gson.annotations.SerializedName

data class UploadMarksClassEntryResponse (
    @SerializedName("status")
    val status: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<Any>
)