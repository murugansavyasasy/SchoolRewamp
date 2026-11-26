package com.vs.schoolmessenger.School.Library.BookStudentsModel

data class StudentBookDetail(
    val book_name: String,
    val book_id: String,
    val book_ref: String,
    val validity_date: String,
    val due_days: Int,
    val category_name: String,
    val author: String,
    val publisher: String,
    val shelf_number: String,
    val edition: String,
    val book_cost: String,
    val issued_on: String,
    val classification: String,
    val language_name: String
)