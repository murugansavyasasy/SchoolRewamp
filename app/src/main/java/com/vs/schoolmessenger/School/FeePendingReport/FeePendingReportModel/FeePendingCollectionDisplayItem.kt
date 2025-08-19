package com.vs.schoolmessenger.School.FeePendingReport.FeePendingReportModel

sealed class FeePendingCollectionDisplayItem {
    data class Header(
        val category: String,
        val total: String,
        val feeList: List<Fee>

    ) : FeePendingCollectionDisplayItem()

    data class Fee(
        val typeName: String,
        val amount: String
    ) : FeePendingCollectionDisplayItem()
}
