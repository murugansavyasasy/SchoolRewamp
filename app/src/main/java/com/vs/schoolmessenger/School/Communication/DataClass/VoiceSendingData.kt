package com.vs.schoolmessenger.School.Communication.DataClass

data class VoiceSendingData(
    val isClickType: Int,
    val selectedDates: List<String>,
    val isStartTimeText: String,
    val isEndTimeText: String,
    val title: String,
    val isEmergency: Boolean,
    val isScheduleCall: Boolean,
    val isAwsUrl: String,
    val isFileName: String,
)
