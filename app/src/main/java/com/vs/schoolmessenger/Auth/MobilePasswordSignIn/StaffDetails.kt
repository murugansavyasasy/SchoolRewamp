package com.vs.schoolmessenger.Auth.MobilePasswordSignIn

data class StaffDetails(  val staff_id: String,
                          val staff_name: String,
                          val school_id: String,
                          val school_name: String,
                          val school_name_regional: String,
                          val city: String,
                          val school_logo: String,
                          val role: String,
                          val is_payment_pending: String,
                          val schedule_call_type: Int,
                          val biometric_enable: Int,
                          val allow_video_download: Boolean)
