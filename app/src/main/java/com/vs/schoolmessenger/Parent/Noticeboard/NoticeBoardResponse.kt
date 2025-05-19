package com.vs.schoolmessenger.Parent.Noticeboard

data class NoticeBoardResponse (
    val status: Boolean,
    val message: String,
    val data: List<Notice>
)