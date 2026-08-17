package com.vs.schoolmessenger.School.ExamReview.ApiResponseModel

import com.google.gson.annotations.SerializedName

data class MarkExamAnaysis(
    @SerializedName("id") val id: String = "",
    @SerializedName("exam_name") val exam_name: String = "",
    @SerializedName("obtained_mark") val obtained_mark: String = "",
    @SerializedName("max_mark") val max_mark: String = "",
    @SerializedName("attendance") val attendance: String = "",
    @SerializedName("remarks") val remarks: String = "",
    @SerializedName("activity_name") val activity_name: String = "",
    @SerializedName("exam_date") val exam_date: String = "",
    @SerializedName("session") val session: String = "",
    @SerializedName("min_mark") val min_mark: String = "",
    @SerializedName("syllabus") val syllabus: String = "",
    @SerializedName("is_publish") val is_publish: String = ""
)