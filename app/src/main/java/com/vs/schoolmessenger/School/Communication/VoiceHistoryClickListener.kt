package com.vs.schoolmessenger.School.Communication

import com.vs.schoolmessenger.School.NoticeBoard.NoticeData

interface VoiceHistoryClickListener {
    fun onItemClick(data: VoiceHistoryData, holder: VoiceHistoryAdapter.DataViewHolder)
}