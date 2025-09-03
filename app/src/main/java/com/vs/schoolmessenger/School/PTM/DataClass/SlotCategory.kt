package com.vs.schoolmessenger.School.PTM.DataClass

data class SlotCategory(
    val today: List<SlotGroup>,
    val upcoming: List<SlotGroup>,
    val completed: List<SlotGroup>
)

