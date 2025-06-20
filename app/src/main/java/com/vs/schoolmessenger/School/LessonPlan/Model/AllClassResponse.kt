package com.vs.schoolmessenger.School.LessonPlan.Model

data class AllClassResponse (
    val status : Boolean,
    val message : String,
    val data : List<AllClassData>
)