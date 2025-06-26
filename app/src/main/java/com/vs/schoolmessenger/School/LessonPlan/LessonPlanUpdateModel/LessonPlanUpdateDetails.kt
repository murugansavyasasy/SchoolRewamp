package com.vs.schoolmessenger.School.LessonPlan.LessonPlanUpdateModel

import java.io.Serializable

data class LessonPlanUpdateDetails (
    val txtLocation: String,
    val txtTitle: String,
    val txtDesc: String,
    val txtStartDate: String,
    val txtStartTime: String
) : Serializable