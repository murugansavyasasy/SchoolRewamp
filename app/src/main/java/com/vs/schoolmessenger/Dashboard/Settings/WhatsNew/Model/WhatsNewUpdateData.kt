package com.vs.schoolmessenger.Dashboard.Settings.WhatsNew.Model

data class WhatsNewUpdateData(
    val id: Int,
    val name: String,
    val description: String,
    val app_redirect_link: String,
    val video_link: String,
    val downloadable_image: String
)