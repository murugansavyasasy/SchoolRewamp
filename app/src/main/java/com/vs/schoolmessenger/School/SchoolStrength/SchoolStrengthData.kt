package com.vs.schoolmessenger.School.SchoolStrength

data class SchoolStrengthData(
    val total_student_strength: String,
    val total_staff_strength: String,
    val standards: List<Standard>
)