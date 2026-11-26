package com.vs.schoolmessenger.School.Library.Model

data class IssuedBooksResponse (
    val status: Boolean,
    val message: String,
    val data: List<IssuedBookData>
)