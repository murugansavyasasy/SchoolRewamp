package com.vs.schoolmessenger.Dashboard.Fragments.Model

data class ProfileField (
    val title: String,
    val type: String,
    val value: String?,
    val is_editable: Boolean,
    val optional: Boolean,
    val node: String
)