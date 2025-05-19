package com.vs.schoolmessenger.School.FeePendingReport

import com.vs.schoolmessenger.School.DailyCollection.DailyCollectionItem

data class FeePendingReportResponse (
    val status: Boolean,
    val message: String,
    val data: List<FeePendingCollectionItem>
)