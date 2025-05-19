package com.vs.schoolmessenger.School.FeePendingReport


data class FeePendingCollectionItem (
    val category: String?,
    val total: String?,
    val pending_data: List<FeePendingData>?,
    val total_pending: String
)