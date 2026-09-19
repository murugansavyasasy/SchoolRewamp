package com.vs.schoolmessenger.School.StudentReport.ProfileUpdateModel

data class StudentProfileUpdateResponse(
    val status: Boolean,
    val message: String,
    val data: List<Any>
)