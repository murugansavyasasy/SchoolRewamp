package com.vs.schoolmessenger.School.SchoolStrength

data class SchoolStrengthResponse (
    val status: Boolean,
    val message: String,
    val data: List<SchoolStrengthData>
)