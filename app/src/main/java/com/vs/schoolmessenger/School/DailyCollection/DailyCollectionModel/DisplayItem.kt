package com.vs.schoolmessenger.School.DailyCollection.DailyCollectionModel

sealed class DisplayItem {
    data class Header(val category: String, val total: String) : DisplayItem()
    data class Fee(val typeName: String, val amount: String) : DisplayItem()
}