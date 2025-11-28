package com.vs.schoolmessenger.School.PTM.DataClass

data class BookedSlotResponse(
    val status: Boolean,
    val message: String,
    val data: List<BookedSlotData>
)
