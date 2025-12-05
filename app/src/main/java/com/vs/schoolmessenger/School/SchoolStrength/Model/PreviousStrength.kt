package com.vs.schoolmessenger.School.SchoolStrength.Model

data class PreviousStrength(
    val aca_year: String,
    val total_staff_strength: String,
    val total_male_staffs_strength: String,
    val total_female_staffs_strength: String,
    val total_other_staffs_strength: String,
    val total_student_strength: String,
    val total_boys_strength: String,
    val total_girls_strength: String,
    val total_others_strength: String,
    val message: String
)