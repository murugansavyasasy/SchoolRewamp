package com.vs.schoolmessenger.School.ExamReview.ApiResponseModel

import com.google.gson.annotations.SerializedName

data class SubjectExamAnalysis(
    @SerializedName("subject_id") val subject_id: String = "",
    @SerializedName("subject_name") val subject_name: String = "",
    @SerializedName("total_marks") val total_marks: String = "",
    @SerializedName("obtained_marks") val obtained_marks: String = "",
    @SerializedName("percentage") val percentage: String = "",
    @SerializedName("marks") val marks: List<MarkExamAnaysis> = emptyList()
)