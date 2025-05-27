package com.vs.schoolmessenger.School.AbsenteesReport.Model

data class AbsenteesResponse(
    val status: Boolean,
    val message: String,
    val data: List<AbsenteeData>
)
