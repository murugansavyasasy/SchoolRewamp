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
//Before
//package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProof.FeeBreakDownCustom
//
///**
// * Three-tier model shared by ALL six sections:
// *
// *   SectionHeader          "Carryover (2025-2026)   ₹6,006.00"   (collapsible — the only expand/collapse state)
// *     └── GroupRow         "Arrear fees              ₹2,000.00"  (bold, always shown once parent is expanded)
// *           └── ChildRow   "– Term 2                 ₹1,000.00"  (indented leaf)
// *           └── ChildRow   "– Term 1                 ₹1,000.00"
// *
// * Sections with no natural grouping (Term Fees, Quantity Fees) skip GroupRow
// * entirely — their ChildRow.groupId is null and they sit directly under the
// * SectionHeader with no dash prefix, matching the flat list in those screens.
// */
//sealed class FeeItem {
//
//    data class SectionHeader(
//        val sectionId: String,
//        val title: String,
//        val totalAmount: String,
//        val iconRes: String,
//        val isExpanded: Boolean,
//        val childCount: Int
//    ) : FeeItem()
//
//    data class GroupRow(
//        val parentSectionId: String,
//        val groupId: String,     // unique within the section: fee_id, route_id, hostel key, etc.
//        val label: String,       // fee_name / "route · fee type" / "hostel (room, bed)"
//        val amount: String
//    ) : FeeItem()
//
//    data class ChildRow(
//        val parentSectionId: String,
//        val groupId: String?,    // null => renders directly under the header, no GroupRow above it
//        val label: String,
//        val amount: String
//    ) : FeeItem()
//}