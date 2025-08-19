package com.vs.schoolmessenger.School.SchoolStrength.Model

import com.google.gson.annotations.SerializedName

data class SchoolData(
    @SerializedName("total_student_strength")
    val totalStudentStrength: String,

    @SerializedName("total_staff_strength")
    val totalStaffStrength: String,

    @SerializedName("total_boys_strength")
    val totalBoysStrength: String,

    @SerializedName("total_girls_strength")
    val totalGirlsStrength: String,

    @SerializedName("total_others_strength")
    val totalOthersStrength: String,

    val standards: List<Standard>
)