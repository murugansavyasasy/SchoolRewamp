package com.vs.schoolmessenger.School.PTM.DataClass

data class BookedSlotData(
    val today: List<BookedSlotItem>,
    val upcoming: List<BookedSlotItem>,
    val completed: List<BookedSlotItem>
)