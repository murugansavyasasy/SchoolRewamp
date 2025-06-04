package com.vs.schoolmessenger.School.Event.Model

import java.io.Serializable

data class EventDetails (
    val title: String,
    val description: String,
    val txtStartDate: String,
    val txtEndDate: String
) : Serializable