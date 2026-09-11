package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProof.FeeBreakDownCustom

/**
 * Three-tier model shared by ALL sections. feeAmount/discountAmount/paidAmount
 * on GroupRow and ChildRow are what item_fee_group.xml / item_fee_child.xml's
 * breakdown_row binds to — FeeAdapter already reads these three fields, so
 * FeeItemMapper MUST populate them on every row it builds, or the breakdown
 * row silently stays hidden (all-null is the adapter's "hide this line" case).
 */
sealed class FeeItem {

    data class SectionHeader(
        val sectionId: String,
        val title: String,
        val totalAmount: String,
        val iconRes: String,
        val isExpanded: Boolean,
        val childCount: Int
    ) : FeeItem()

    data class GroupRow(
        val parentSectionId: String,
        val groupId: String,
        val label: String,
        val amount: String,
        val feeAmount: String? = null,
        val discountAmount: String? = null,
        val paidAmount: String? = null
    ) : FeeItem()

    data class ChildRow(
        val parentSectionId: String,
        val groupId: String?,
        val label: String,
        val amount: String,
        val feeAmount: String? = null,
        val discountAmount: String? = null,
        val paidAmount: String? = null
    ) : FeeItem()
}
