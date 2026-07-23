package com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewampModel

import com.google.gson.annotations.SerializedName

data class ExamTimetableResponse(
    @SerializedName("status")
    val status: Boolean,

    @SerializedName("message")
    val message: String?,

    @SerializedName("data")
    val data: List<ExamTimetable>?
)