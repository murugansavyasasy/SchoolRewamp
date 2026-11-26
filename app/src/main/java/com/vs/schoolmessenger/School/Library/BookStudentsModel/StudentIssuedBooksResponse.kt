package com.vs.schoolmessenger.School.Library.BookStudentsModel

class StudentIssuedBooksResponse (
    val status: Boolean,
    val message: String,
    val data: List<StudentIssuedBookData>
    )