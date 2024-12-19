package com.vs.schoolmessenger.School.LessonPlan


interface LessonPlanClickListener {
    fun onEditItem(data: LessonPlanData)
    fun onDeleteItem(data: LessonPlanData)
}