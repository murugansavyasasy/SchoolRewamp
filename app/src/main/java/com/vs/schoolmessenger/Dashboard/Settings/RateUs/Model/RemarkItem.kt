package com.vs.schoolmessenger.Dashboard.Settings.RateUs.Model

data class RemarkItem(
    val name: String? = null,          // Example: "What went wrong?"
    val value: String? = null,          // Example: "What went wrong?"
    val rating: Int? = null,           // 1,2,3,4,5
    val category: List<CategoryItem>? = null
)