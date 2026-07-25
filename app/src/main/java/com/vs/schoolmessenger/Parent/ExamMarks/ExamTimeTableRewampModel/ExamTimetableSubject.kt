package com.vs.schoolmessenger.Parent.ExamMarks.ExamTimeTableRewampModel

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExamTimetableSubject(

    @SerializedName("subject_id")
    val subjectId: Int?,

    @SerializedName("subject_name")
    val subjectName: String?,

    @SerializedName("total_mark")
    val total_mark: String?,

    @SerializedName("activities")
    val activities: List<ExamTimetableActivity>?

) : Parcelable