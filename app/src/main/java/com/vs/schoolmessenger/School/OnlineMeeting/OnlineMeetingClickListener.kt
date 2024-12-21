package com.vs.schoolmessenger.School.OnlineMeeting

interface OnlineMeetingClickListener {
    fun onItemReminderClick(data: OnlineMeetingHistoryData)
    fun onItemLinkClick(data: OnlineMeetingHistoryData)
}