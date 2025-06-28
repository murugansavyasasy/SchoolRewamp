package com.vs.schoolmessenger.School.LessonPlan.LessonPlanSummaryModel

data class AllClassResponse(
    val status: Boolean,
    val message: String,
    val data: List<AllClassData>
)