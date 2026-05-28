package com.vs.schoolmessenger.School.FeePendingReport.FeePendingReportModel

data class FeePendingItem(
    val category: String,
    val total: String,
    val pending_data: List<FeePendingData>
)
