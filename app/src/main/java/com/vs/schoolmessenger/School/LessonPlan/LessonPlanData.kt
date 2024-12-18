package com.vs.schoolmessenger.School.LessonPlan

data class LessonPlanData(
    val Title: String,
    val FromTime: String,
    val ToTime: String,
    val Unit: String,
    val Remarks: String,
    val Status:Int
)
