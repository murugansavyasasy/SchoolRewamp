package com.vs.schoolmessenger.School.LessonPlan.LessonPlanUpdateModel

data class LessonPlanUpdateResponse(
    val status: Boolean,
    val message: String,
    val data: List<Any>
)