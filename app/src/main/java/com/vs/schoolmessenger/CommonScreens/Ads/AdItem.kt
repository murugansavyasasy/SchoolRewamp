package com.vs.schoolmessenger.CommonScreens.Ads

data class AdItem(
    val id: Int? = null,
    val name: String? = null,
    val content_url: String? = null,
    val redirect_url: String? = null,
    val ads_display_options: AdsDisplayOptions? = null
)