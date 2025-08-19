package com.vs.schoolmessenger.School.Event.Model

import java.io.Serializable

data class EventDetails(
    val txtLocation: String,
    val txtTitle: String,
    val txtDesc: String,
    val txtStartDate: String,
    val txtStartTime: String,
    val isCategory: String,
) : Serializable