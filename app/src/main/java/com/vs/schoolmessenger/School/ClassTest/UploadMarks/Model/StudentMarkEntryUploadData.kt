package com.vs.schoolmessenger.School.ClassTest.UploadMarks.Model

import com.google.gson.annotations.SerializedName

data class StudentMarkEntryUploadData (
    @SerializedName("student_id")
    val studentId: String,

    @SerializedName("admission_no")
    val admissionNo: String,

    @SerializedName("roll_no")
    val rollNo: String,

    @SerializedName("student_name")
    val studentName: String,

    @SerializedName("attendance")
    val attendance: String,

    @SerializedName("mark")
    val mark: String,

    @SerializedName("remarks")
    val remarks: String
)