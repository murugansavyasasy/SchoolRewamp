package com.vs.schoolmessenger.Dashboard.Fragments.Model

sealed class ProfileItem {
    data class Header(val title: String) : ProfileItem()
    data class Field(val field: ProfileField) : ProfileItem()
}
