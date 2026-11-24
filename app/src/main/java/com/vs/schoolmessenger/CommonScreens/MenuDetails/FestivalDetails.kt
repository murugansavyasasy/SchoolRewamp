package com.vs.schoolmessenger.CommonScreens.MenuDetails

/**
 * "festival_details" from the menus (dashboard) API. Shown in a popup when "is_festival" is true.
 * Every field may be empty / missing.
 */
data class FestivalDetails(
    val date: String? = null,     // "2026-11-24"
    val name: String? = null,     // "Tuesday holiday"
    val content: String? = null,  // message text (optional)
    val url: String? = null       // festival image (optional)
)
