package com.vs.schoolmessenger.School.LessonPlan

import com.vs.schoolmessenger.School.LessonPlan.LessonPlanViewSummaryModel.LessonPlanViewSummaryItem


interface LessonPlanClickListener {
    fun onEditItem(data: LessonPlanViewSummaryItem)
    fun onDeleteItem(data: LessonPlanData)
}