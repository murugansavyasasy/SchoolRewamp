package com.vs.schoolmessenger.Parent.ClassTestMark.DataClass

import com.google.gson.annotations.SerializedName

data class MarkSubjectData(
    @SerializedName("subject_id") val subjectId: String,
    @SerializedName("subject_name") val subjectName: String,
    @SerializedName("activities") val activities: List<MarkActivityData>
)