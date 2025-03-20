package com.vs.schoolmessenger.Auth.MobilePasswordSignIn

data class ChildDetails(
    val child_id: String,
    val child_name: String,
    val standard_name: String,
    val section_name: String,
    val school_id: String,
    val school_name: String,
    val school_name_regional: String,
    val school_city: String,
    val school_logo_url: String,
    val roll_number: String,
    val display_message: String,
    val class_id: Int,
    val section_id: Int
)
