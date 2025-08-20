package com.vs.schoolmessenger.School.PTM.DataClass

import java.io.Serializable

data class ClassSection(
    val class_id: String,
    val section_id: String,
    val class_name: String,
    val section_name: String
)  : Serializable
