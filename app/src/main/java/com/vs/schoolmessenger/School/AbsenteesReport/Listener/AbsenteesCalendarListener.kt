package com.vs.schoolmessenger.School.AbsenteesReport.Listener

interface AbsenteesCalendarListener {
    fun onDateSelected(date: String, tag: String)
    fun onMonthChanged(month: Int, year: Int)
}