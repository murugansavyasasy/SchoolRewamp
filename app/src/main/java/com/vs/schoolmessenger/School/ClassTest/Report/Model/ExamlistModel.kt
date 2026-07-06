package com.vs.schoolmessenger.School.ClassTest.Report.Model

import com.google.gson.annotations.SerializedName

data class ExamlistModel (
    @SerializedName("class_test_id")
    val classTestId: String,

    @SerializedName("exam_name")
    val examName: String,

    @SerializedName("sent_by")
    val sentBy: String,

    @SerializedName("sent_on")
    val sentOn: String,

    @SerializedName("sections")
    val sections: List<SectionModeldata>
)