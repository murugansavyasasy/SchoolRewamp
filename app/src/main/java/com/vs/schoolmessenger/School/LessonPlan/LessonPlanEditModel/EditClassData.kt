package com.vs.schoolmessenger.School.LessonPlan.LessonPlanEditModel

data class EditClassData(
    val id: String,
    val name: String,
    val field_id: String,
    var value: String,
    val field_type: String,
    val field_data: List<String>,
    val is_disable: Boolean
)