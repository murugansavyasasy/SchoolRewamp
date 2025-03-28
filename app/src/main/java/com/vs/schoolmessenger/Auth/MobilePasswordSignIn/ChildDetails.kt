package com.vs.schoolmessenger.Auth.MobilePasswordSignIn

data class ChildDetails(
    val child_id: String,
    val name: String,
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
    val section_id: Int,
    val access_token: String,
    val email: String,
    val father_name: String,
    val mother_name: String,
    val father_occupation: String,
    val mother_occupation: String,
    val blood_group: String,
    val student_address: String,
    val secondary_mobile: String,
    val whatsapp_number: String,
    val class_teacher: String

)
