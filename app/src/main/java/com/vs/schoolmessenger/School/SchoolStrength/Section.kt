package com.vs.schoolmessenger.School.SchoolStrength

data class Section(
    val id: String,
    val name: String,
    val level: Int,
    val boys_count: Int,
    val girls_count: Int,
    val other_count: Int,
    val total_students: String
)