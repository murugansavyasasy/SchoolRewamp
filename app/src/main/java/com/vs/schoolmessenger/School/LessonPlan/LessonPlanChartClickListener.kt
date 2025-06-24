package com.vs.schoolmessenger.School.LessonPlan

import com.vs.schoolmessenger.School.LessonPlan.Model.AllClassData

interface LessonPlanChartClickListener {
    fun onItem(data: AllClassData)
}