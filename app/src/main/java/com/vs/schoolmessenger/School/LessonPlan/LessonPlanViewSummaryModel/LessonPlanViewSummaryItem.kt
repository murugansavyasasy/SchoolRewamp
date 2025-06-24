package com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummaryModel

data class LessonPlanViewSummaryItem (
    val particular_id: String,
    val lesson_plan_status: Int,
    val details: List<LessonPlanViewSummaryDetail>
)