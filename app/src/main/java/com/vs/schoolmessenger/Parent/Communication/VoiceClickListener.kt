package com.vs.schoolmessenger.Parent.Communication


interface VoiceClickListener {
    fun onItemClick(data: VoiceData, holder: VoiceAdapter.DataViewHolder)
    fun onUpdateArchiveStatus(type: String?, detailId: String?)
}