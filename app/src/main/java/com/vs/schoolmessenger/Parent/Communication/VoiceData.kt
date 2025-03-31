package com.vs.schoolmessenger.Parent.Communication

data class VoiceData(
    val isType: String,
    val isDate: String,
    val title: String,
    var content: String,
    val url: String
)
