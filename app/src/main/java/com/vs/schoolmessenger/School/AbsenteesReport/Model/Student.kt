package com.vs.schoolmessenger.School.AbsenteesReport.Model

import com.google.gson.annotations.SerializedName

data class Student(
    @SerializedName("student_id") val student_id: String,
    @SerializedName("student_name") val student_name: String,
    @SerializedName("admission_no") val admission_no: String,
    @SerializedName("attd_status") val att_status: String?,
    @SerializedName("gender") val gender: String,
    @SerializedName("roll_no") val roll_no: String,
    @SerializedName("photo_path") val photo_path: String?,
    @SerializedName("primary_mobile") val primary_mobile: String
)