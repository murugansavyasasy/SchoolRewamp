package com.vs.schoolmessenger.School.Communication.Interface

import com.vs.schoolmessenger.School.Communication.Adapter.VoiceHistoryAdapter
import com.vs.schoolmessenger.School.Communication.DataClass.VoiceHistoryDetails


interface VoiceHistoryClickListener {
    fun onItemClick(data: VoiceHistoryDetails, holder: VoiceHistoryAdapter.DataViewHolder)
}