package com.vs.schoolmessenger.School.Communication

data class VoiceSendingData(
    val isFileUploaded: String?,
    val isClickType: Int,
    val selectedDates: List<String>,
    val isStartTimeText: String,
    val isEndTimeText: String,
    val title: String,
    val isEmergency: Int,
    val isScheduleCall: Boolean
)
