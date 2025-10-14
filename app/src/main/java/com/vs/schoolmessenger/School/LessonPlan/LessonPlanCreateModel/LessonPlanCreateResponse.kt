package com.vs.schoolmessenger.School.LessonPlan.LessonPlanCreateModel

data class LessonPlanCreateResponse (
    val status: Boolean,
    val message: String,
    val data: List<Any>
    )