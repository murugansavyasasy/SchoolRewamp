package com.vs.schoolmessenger.Parent.RequestLeave

import java.util.Date

data class CalendarDay(
    val date: Date,
    val isCurrentMonth: Boolean,
    var isSelected: Boolean = false
)
