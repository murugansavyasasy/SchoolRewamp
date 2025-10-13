package com.vs.schoolmessenger.School.LessonPlan.LessonPlanCreateModel

data class LessonPlanTemplateResponse (
    val status: Boolean,
    val message: String,
    val data: List<LessonPlanTemplate>
)