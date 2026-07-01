package com.vs.schoolmessenger.Parent.ClassTestMark.DataClass

import com.google.gson.annotations.SerializedName

data class MarkData(
    @SerializedName("class_test_id") val classTestId: String,
    @SerializedName("exam_name") val examName: String,
    @SerializedName("over_all_student_marks") val overallStudentMarks: String,
    @SerializedName("over_all_Marks") val overallMarks: String,
    @SerializedName("over_all_persentage") val overallPercentage: String,
    @SerializedName("subjects") val subjects: List<MarkSubjectData>
)