package com.vs.schoolmessenger.School.StudentReport.StudentFeeDetails

/**
 * UI-only models. The adapter never touches the API models directly,
 * so design changes only affect FeeUiMapper (what text is shown) and the adapter/layouts (how it looks).
 */
enum class FeeStatus { PAID, PARTIAL, PENDING }

/** Position of a card inside its white section container (drives corner rounding). */
enum class CardPosition { SINGLE, FIRST, MIDDLE, LAST }

data class FeeChild(
    val title: String,      // e.g. "Payment 1 · 15/10/2026"
    val subtitle: String    // e.g. "₹913.00 · Cash"
)

sealed class FeeListItem {

    data class Summary(
        val total: String,
        val paid: String,
        val pending: String,
        val discount: String,
        val percent: java.math.BigDecimal   // 0.0 - 100.0 (one decimal)
    ) : FeeListItem()

    data class SectionHeader(
        val title: String,      // "Term fees"
        val pending: String     // "₹25,000.00"
    ) : FeeListItem()

    data class Card(
        val title: String,
        val subtitle: String?,          // "Term 1", "2024-2025", "Room 3 · Bed B2"
        val details: String,            // "Fee ₹x · Paid ₹y · Discount ₹z"
        val pending: String,
        val status: FeeStatus,
        val children: List<FeeChild>,
        var expanded: Boolean,
        val position: CardPosition
    ) : FeeListItem()
}