package com.vs.schoolmessenger.Parent.InteractionWithStaff

import java.time.LocalDate

data class DateModel(
    val date: LocalDate?,
    var isSelected: Boolean = false
)