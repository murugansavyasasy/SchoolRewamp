package com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewamp

import com.google.gson.annotations.SerializedName

data class ExamTimetable(

    @SerializedName("exam_id")
    val examId: Int?,

    @SerializedName("exam_name")
    val examName: String?,

    @SerializedName("subjects")
    val subjects: List<ExamTimetableSubject>?
)
