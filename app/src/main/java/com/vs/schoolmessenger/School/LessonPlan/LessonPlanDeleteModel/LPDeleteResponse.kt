package com.vs.schoolmessenger.School.LessonPlan.LessonPlanDeleteModel

import com.vs.schoolmessenger.School.LessonPlan.LessonPlanEditModel.EditClassData

data class LPDeleteResponse (
    val status: Boolean,
    val message: String,
    val data: List<Any>
)