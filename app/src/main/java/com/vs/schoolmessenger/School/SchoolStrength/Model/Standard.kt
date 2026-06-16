package com.vs.schoolmessenger.School.SchoolStrength.Model

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