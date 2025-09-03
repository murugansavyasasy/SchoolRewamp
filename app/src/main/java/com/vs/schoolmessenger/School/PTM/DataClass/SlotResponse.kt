package com.vs.schoolmessenger.School.PTM.DataClass

data class SlotResponse(
    val status: Boolean,
    val message: String,
    val data: List<SlotCategory>
)
