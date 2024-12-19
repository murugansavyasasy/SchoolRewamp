package com.vs.schoolmessenger.School.Event

import com.vs.schoolmessenger.School.NoticeBoard.NoticeData

interface EventClickListener {
    fun onItemClick(data: EventHistoryData)
}