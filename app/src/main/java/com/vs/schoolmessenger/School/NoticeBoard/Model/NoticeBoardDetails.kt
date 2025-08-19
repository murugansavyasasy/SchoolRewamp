package com.vs.schoolmessenger.School.NoticeBoard.Model

import java.io.Serializable

data class NoticeBoardDetails(
    val title: String,
    val description: String,
    val txtStartDate: String,
    val txtEndDate: String
) : Serializable