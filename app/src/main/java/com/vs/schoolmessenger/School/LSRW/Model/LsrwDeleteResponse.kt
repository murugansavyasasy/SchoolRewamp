package com.vs.schoolmessenger.School.LSRW.Model

data class LsrwDeleteResponse(
    val status: Boolean,
    val message: String,
    val data: List<Any>
)