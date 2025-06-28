package com.vs.schoolmessenger.School.LessonPlan.LessonPlanDeleteModel


data class LPDeleteResponse(
    val status: Boolean,
    val message: String,
    val data: List<Any>
)