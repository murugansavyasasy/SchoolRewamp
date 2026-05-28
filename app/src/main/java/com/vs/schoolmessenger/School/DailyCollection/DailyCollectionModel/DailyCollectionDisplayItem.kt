package com.vs.schoolmessenger.School.DailyCollection.DailyCollectionModel

sealed class DailyCollectionDisplayItem {
    data class Header(
        val category: String,
        val total: String,
        val feeList: List<Fee>
    ) : DailyCollectionDisplayItem()

    data class Fee(
        val typeName: String,
        val amount: String
    ) : DailyCollectionDisplayItem()
}
