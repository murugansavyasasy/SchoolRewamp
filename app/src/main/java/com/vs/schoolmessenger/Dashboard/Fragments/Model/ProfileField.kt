package com.vs.schoolmessenger.Dashboard.Fragments.Model

data class ProfileField(
    val title: String,
    val type: String,
    var value: String?,
    val is_editable: Boolean,
    val optional: Boolean,
    val options: List<String>?,
    val node: String,
    val originalValue: String? = value
)
