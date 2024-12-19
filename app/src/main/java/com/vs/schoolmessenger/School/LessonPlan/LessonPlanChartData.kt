package com.vs.schoolmessenger.School.LessonPlan

data class LessonPlanChartData(

    val Subject: String,
    val Section: String,
    val StaffName: String,
    val Status: String,
    val Pending: Int,
    val Completed: Int
)
