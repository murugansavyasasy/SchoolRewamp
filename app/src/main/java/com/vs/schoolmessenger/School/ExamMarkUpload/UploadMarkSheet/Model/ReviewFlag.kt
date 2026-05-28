package com.vs.schoolmessenger.School.ExamMarkUpload.UploadMarkSheet.Model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReviewFlag(
    @SerializedName("student_id")
    val studentId: String,
    val field: String,
    val value: String,
    val reason: String
) : Parcelable