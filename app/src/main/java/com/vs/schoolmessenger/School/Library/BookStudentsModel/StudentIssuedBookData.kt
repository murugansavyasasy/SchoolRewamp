package com.vs.schoolmessenger.School.Library.BookStudentsModel

data class StudentIssuedBookData (
    val student_id: String,
    val admission_no: String,
    val student_name: String,
    val class_name: String,
    val section_name: String,
    val no_of_books: Int,
    val book_details: List<StudentBookDetail>
)