package com.vs.schoolmessenger.Auth.MobilePasswordSignIn

data class UserDetails(

    val is_staff: Boolean,
    val staff_role: String,
    val role_name: String,
    val staff_details: List<StaffDetails>,
    val is_parent: Boolean,
    val child_details: List<ChildDetails>,
    val max_general_sms_count: Int,
    val max_homework_sms_count: Int,
    val max_emergency_voice_duration: Int,
    val max_general_voice_duration: Int,
    val max_hw_voice_duration: Int,
    val image_count: Int
)
