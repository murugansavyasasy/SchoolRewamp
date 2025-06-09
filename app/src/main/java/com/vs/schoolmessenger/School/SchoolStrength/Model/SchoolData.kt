package com.vs.schoolmessenger.School.SchoolStrength.Model

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.School.SchoolStrength.Model.Standard

data class SchoolData(
    @SerializedName("total_student_strength")
    val totalStudentStrength: String,

    @SerializedName("total_staff_strength")
    val totalStaffStrength: String,

    val standards: List<Standard>
)