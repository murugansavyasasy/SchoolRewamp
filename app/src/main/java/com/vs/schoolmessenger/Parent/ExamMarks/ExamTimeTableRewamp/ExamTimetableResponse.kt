package com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewamp

import com.google.gson.annotations.SerializedName

data class ExamTimetableResponse(
    @SerializedName("status")
    val status: String?,

    @SerializedName("message")
    val message: String?,

    @SerializedName("data")
    val data: List<ExamTimetable>?
)