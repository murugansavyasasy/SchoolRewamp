package com.vs.schoolmessenger.School.LessonPlan.LessonPlanSummary

import com.vs.schoolmessenger.School.LessonPlan.LessonPlanSummaryModel.AllClassData

interface LessonPlanChartClickListener {
    fun onItem(data: AllClassData)
}