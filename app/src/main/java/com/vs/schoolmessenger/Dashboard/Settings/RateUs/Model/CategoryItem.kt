package com.vs.schoolmessenger.Dashboard.Settings.RateUs.Model

data class CategoryItem(
    val name: String? = null,          // Example: "Limited amenities"
    var selected: Boolean? = null      // Must be var so you can toggle UI
)