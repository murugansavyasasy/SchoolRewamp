package com.vs.schoolmessenger.School.NoticeBoard.Response

data class NoticeBoardDeleteResponse (
    val status: Boolean,
    val message: String,
    val data: List<Any>
)