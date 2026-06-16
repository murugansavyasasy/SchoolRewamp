package com.vs.schoolmessenger.School.DailyCollection.DailyCollectionModel


data class DailyData(
    val collections: List<DailyCollectionItem>,
    val total_collection: String
)