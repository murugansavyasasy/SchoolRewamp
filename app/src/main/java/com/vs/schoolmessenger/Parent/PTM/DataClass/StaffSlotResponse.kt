package com.vs.schoolmessenger.Parent.PTM.DataClass

data class StaffSlotResponse(
    val status: Boolean,
    val message: String,
    val data: List<StaffSlot>
)
