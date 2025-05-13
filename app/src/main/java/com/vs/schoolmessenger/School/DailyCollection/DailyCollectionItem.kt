package com.vs.schoolmessenger.School.DailyCollection

class DailyCollectionItem (
        val category: String,
        val total: String,
        val fee_data: List<FeeData>,
        val total_collection: String
)

//Existing --Default
//package com.vs.schoolmessenger.School.DailyCollection
//
//sealed class DailyCollectionI`tem {
//    data class CategoryData(
//        val category: String,
//        val total: String,
//        val fee_data: List<FeeData>
//    ) : DailyCollectionItem()
//
//    data class TotalCollection(
//        val total_collection: String
//    ) : DailyCollectionItem()
//}