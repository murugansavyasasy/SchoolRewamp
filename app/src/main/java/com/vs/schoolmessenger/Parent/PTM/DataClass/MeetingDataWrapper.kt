package com.vs.schoolmessenger.Parent.PTM.DataClass

data class MeetingDataWrapper(
    val today: List<MeetingItem>,
    val upcoming: List<MeetingItem>,
    val completed: List<MeetingItem>
)
