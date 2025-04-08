package com.vs.schoolmessenger.School.Communication

import com.vs.schoolmessenger.School.NoticeBoard.NoticeData

interface VoiceHistoryClickListener {
    fun onItemClick(data: VoiceHistoryDetails, holder: VoiceHistoryAdapter.DataViewHolder)
}