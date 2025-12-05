package com.vs.schoolmessenger.School.PTM.DataClass

data class AvailableSlotGroup(
    val date: String,
    val slots: MutableList<SlotAvailability>
)
