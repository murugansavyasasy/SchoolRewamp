package com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewampModel

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExamTimetable(

    @SerializedName("exam_id")
    val examId: Int?,

    @SerializedName("exam_name")
    val examName: String?,

    @SerializedName("subjects")
    val subjects: List<ExamTimetableSubject>?

) : Parcelable