package com.vs.schoolmessenger.School.DailyCollection.DailyCollectionModel

import com.vs.schoolmessenger.School.DailyCollection.DailyCollectionModel.DailyCollectionItem

data class CollectionData(
    val collections: List<DailyCollectionItem>,
    val total_collection: String
)