package com.vs.schoolmessenger.School.LessonPlan.LessonPlanEditModel

data class LessonPlanEditResponse (
    val status : String,
    val message : String,
    val data : List<EditClassData>
)