package com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummaryModel

data class LessonPlanViewSummaryResponse(
    val status: Boolean,
    val message: String,
    val data: List<LessonPlanViewSummaryItem>

)