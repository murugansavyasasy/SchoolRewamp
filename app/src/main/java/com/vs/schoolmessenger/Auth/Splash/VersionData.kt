package com.vs.schoolmessenger.Auth.Splash

import com.vs.schoolmessenger.Auth.Country.Country

data class VersionData(
    val update_available: Boolean,
    val force_update: Boolean,
    val new_version: String,
    val new_version_updates: String,
    val country_details: Country,
    val toaster_title: String,
    val play_store_market_id: String,
    val play_store_link: String
)