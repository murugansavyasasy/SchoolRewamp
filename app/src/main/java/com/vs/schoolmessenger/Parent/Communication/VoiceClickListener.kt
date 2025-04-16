package com.vs.schoolmessenger.Parent.Communication


interface VoiceClickListener {
    fun onItemClick(data: VoiceData, holder: UnifiedVoiceAdapter.DataViewHolder)
    fun onUpdateArchiveStatus(type: String?, detailId: String?)
    fun onUpdateCommunicationStatus(type: String?, detailId: String?)

}