package com.vs.schoolmessenger.School.NoticeBoard.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NoticeBoardDetails (
    val title: String,
    val description: String,
    val txtStartDate: String,
    val txtEndDate: String
) : Parcelable