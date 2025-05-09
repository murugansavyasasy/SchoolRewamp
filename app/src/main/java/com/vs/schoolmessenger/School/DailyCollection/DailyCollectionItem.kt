package com.vs.schoolmessenger.School.DailyCollection

sealed class DailyCollectionItem {
    data class CategoryData(
        val category: String,
        val total: String,
        val fee_data: List<FeeData>
    ) : DailyCollectionItem()

    data class TotalCollection(
        val total_collection: String
    ) : DailyCollectionItem()
}