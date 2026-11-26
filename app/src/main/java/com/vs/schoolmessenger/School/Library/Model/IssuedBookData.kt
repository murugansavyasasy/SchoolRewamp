package com.vs.schoolmessenger.School.Library.Model

data class IssuedBookData (
    val staff_id: String,
    val empid: String,
    val staff_name: String,
    val class_name: String,
    val section_name: String,
    val no_of_books: Int,
    val book_details: List<BookDetail>
)