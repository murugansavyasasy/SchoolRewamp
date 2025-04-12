package com.vs.schoolmessenger.School.Communication


interface VoiceHistoryClickListener {
    fun onItemClick(data: VoiceHistoryDetails, holder: VoiceHistoryAdapter.DataViewHolder)
}