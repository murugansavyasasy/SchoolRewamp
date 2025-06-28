package com.vs.schoolmessenger.School.LessonPlan.LessonPlanEditModel

data class LessonPlanEditResponse(
    val status: Boolean,
    val message: String,
    val data: List<EditClassData>
)