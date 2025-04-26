package com.vs.schoolmessenger.Parent.Homework

data class HomeworkResponse(
    val status: Boolean,
    val message: String,
    val data: List<HomeWorkDate>
)

data class HomeWorkDate(
    val date: String,
    var homework: List<HomeWorkDetails>
)

data class HomeWorkDetails(
    val title: String,
    val description: String,
    val subject_name: String,
    val file_path: List<FilePath>
)

data class FilePath(
    val type: String,
    val path: String
)