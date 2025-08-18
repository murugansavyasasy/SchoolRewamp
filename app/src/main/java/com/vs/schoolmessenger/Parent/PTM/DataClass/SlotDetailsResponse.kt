package com.vs.schoolmessenger.Parent.PTM.DataClass

data class SlotDetailsResponse(
    val status: Boolean,
    val message: String,
    val data: List<SlotDetailItem>
)
