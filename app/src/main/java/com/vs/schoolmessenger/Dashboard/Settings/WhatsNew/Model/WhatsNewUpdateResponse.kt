package com.vs.schoolmessenger.Dashboard.Settings.WhatsNew.Model

data class WhatsNewUpdateResponse (
    val status: Boolean,
    val message: String,
    val data: List<WhatsNewUpdateData>
)