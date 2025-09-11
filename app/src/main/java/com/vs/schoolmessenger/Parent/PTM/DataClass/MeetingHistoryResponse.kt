package com.vs.schoolmessenger.Parent.PTM.DataClass

data class MeetingHistoryResponse(
    val status: Boolean,
    val message: String,
    val data: List<MeetingDataWrapper>
)