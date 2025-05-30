package com.vs.schoolmessenger.School.SchoolStrength.Model

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.School.AbsenteesReport.Model.ClassWise

data class Standard(
    val id: String,
    val name: String,
    val level: String,
    val boys_count: String,
    val girls_count: String,
    val other_count: String,
    val total_students: String,
    val sections: List<Section>
)