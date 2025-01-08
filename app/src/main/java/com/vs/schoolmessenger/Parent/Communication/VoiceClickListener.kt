package com.vs.schoolmessenger.Parent.Communication


interface VoiceClickListener {
    fun onItemClick(data: VoiceData, holder: VoiceAdapter.DataViewHolder)
}