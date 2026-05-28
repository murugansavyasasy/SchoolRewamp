package com.vs.schoolmessenger.School.LessonPlan.LessonPlanSummaryModel

data class AllClassData(
    val section_subject_id: String,
    val staff_name: String,
    val class_name: String,
    val section_name: String,
    val subject_name: String,
    val completed_items: String,
    val total_items: String,
    val percentage_value: Int,
    val items_completed: String
)