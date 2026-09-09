package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProof.FeeBreakDownCustom

import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.OthersModel.GenericFee
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofDataModel.PaymentProofFeeDetails
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.CarryOverModel.CarryOverFee
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.CarryOverModel.CarryOverFeeList
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.HostelModel.HostelFee
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.Quantity
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.TermModel.Term
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.TransportModel.TransportFee
import java.math.BigDecimal
import java.text.DecimalFormat
import kotlin.collections.filter


/**
 * Internal shape: one section = header info + its flattened rows (GroupRow/ChildRow),
 * already in display order. Building this once per rebuild keeps buildFlatList() trivial.
 */
private data class Section(
    val id: String,
    val title: String,
    val totalAmount: String,
    val iconRes: String,
    val rows: List<FeeItem>,
    val childCountForBadge: Int
)

object FeeItemMapper {

    fun buildFlatList(feeDetails: PaymentProofFeeDetails, expandedSectionId: String?): List<FeeItem> {
        val sections = buildSections(feeDetails)
        val result = mutableListOf<FeeItem>()

        sections.forEach { section ->
            val isExpanded = section.id == expandedSectionId
            result += FeeItem.SectionHeader(
                sectionId = section.id,
                title = section.title,
                totalAmount = section.totalAmount,
                iconRes = section.iconRes,
                isExpanded = isExpanded,
                childCount = section.childCountForBadge
            )
            if (isExpanded) {
                result += section.rows
            }
        }
        return result
    }

    private fun buildSections(fd: PaymentProofFeeDetails): List<Section> {
        val sections = mutableListOf<Section>()

        buildTermSection(fd.term)?.let { sections += it }
        buildOthersSection(fd.others)?.let { sections += it }
        sections += buildCarryoverSections(fd.carryover)   // one Section PER entry — never merged
        buildTransportSection(fd.transport)?.let { sections += it }
        buildHostelSection(fd.hostel)?.let { sections += it }
        buildQuantitySection(fd.quantity)?.let { sections += it }

        return sections
    }
    // TERM — flat, no grouping. Breakdown comes straight from each fee entry.
    private fun buildTermSection(terms: List<Term>?): Section? {
        if (terms.isNullOrEmpty()) return null

        val rows = terms.flatMap { term ->
            term.fees_details.orEmpty().map { fee ->
                FeeItem.ChildRow(
                    parentSectionId = "TERM",
                    groupId = null,
                    label = "${term.term_name} - ${fee.fee_name}",
                    amount = fee.amount_to_be_paid ?: "",
                    feeAmount = fee.fee_amount,
                    discountAmount = fee.discount_availed,
                    paidAmount = fee.actual_paid
                )
            }
        }
        val total = rows.map { it.amount }.sumAmounts()
        return Section("TERM", "Term Fees", total, "\uD83D\uDCDA", rows, rows.size)
    }

    // OTHERS — grouped by fee_id. Group-level breakdown is summed across the
    // entries in that group (no single API field gives that rollup); child
    // (month) breakdown passes straight through from month_details.
    private fun buildOthersSection(others: List<GenericFee>?): Section? {
        if (others.isNullOrEmpty()) return null

        val groupedByFeeId: Map<String, List<GenericFee>> = others.groupBy { it.fee_id ?: "" }

        val rows = mutableListOf<FeeItem>()
        groupedByFeeId.forEach { (feeId, entriesForFeeId) ->
            val groupLabel = entriesForFeeId.first().fee_name ?: ""
            rows += FeeItem.GroupRow(
                parentSectionId = "OTHERS",
                groupId = feeId,
                label = groupLabel,
                amount = entriesForFeeId.map { it.amount_to_be_paid }.sumAmounts(),
                feeAmount = entriesForFeeId.map { it.amount_to_be_paid }.sumAmounts(),
                discountAmount = entriesForFeeId.map { it.discount_availed }.sumAmounts(),
                paidAmount = entriesForFeeId.map { it.actual_paid }.sumAmounts()
            )
            entriesForFeeId.forEach { entry ->
                entry.month_details.orEmpty().forEach { month ->
                    rows += FeeItem.ChildRow(
                        parentSectionId = "OTHERS",
                        groupId = feeId,
                        label = "– ${month.month_name}",
                        amount = month.amount_to_be_paid ?: "",
                        feeAmount = month.amount_per_month,
                        discountAmount = month.discount_amount,
                        paidAmount = month.paid
                    )
                }
            }
        }
        val total = others.map { it.amount_to_be_paid }.sumAmounts()
        return Section("OTHERS", "Other Fees", total, "\uD83D\uDCC4", rows, groupedByFeeId.size)
    }

    // CARRYOVER — one Section PER array entry, never merged into one header.
    // Each entry's own amount_to_be_paid is the header total — direct
    // pass-through, never re-summed. Grouping by fee_id happens only within
    // that single entry's own carry_over_fee list. Group breakdown is summed
    // across the term entries in that group; child breakdown passes straight
    // through from each carry_over_fee detail.
    private fun buildCarryoverSections(carryover: List<CarryOverFee>?): List<Section> {
        if (carryover.isNullOrEmpty()) return emptyList()

        return carryover.mapIndexed { index, entry ->
            // `id` can repeat across entries (both "2025-2026" and "2024-2025"
            // may share the same id) — index guarantees each header is distinct.
            val sectionId = "CARRYOVER_${index}_${entry.id ?: ""}"

            val allDetails: List<CarryOverFeeList> = entry.carry_over_fee.orEmpty()
            val groupedByFeeId: Map<String, List<CarryOverFeeList>> = allDetails.groupBy { it.fee_id ?: "" }

            val rows = mutableListOf<FeeItem>()
            groupedByFeeId.forEach { (feeId, entriesForFeeId) ->
                val groupLabel = entriesForFeeId.first().fee_name ?: ""
                rows += FeeItem.GroupRow(
                    parentSectionId = sectionId,
                    groupId = feeId,
                    label = groupLabel,
                    amount = entriesForFeeId.map { it.pending_amount }.sumAmounts(),
                    feeAmount = entriesForFeeId.map { it.fee_amount }.sumAmounts(),
                    discountAmount = entriesForFeeId.map { it.discount_amount }.sumAmounts(),
                    paidAmount = entriesForFeeId.map { it.paid_amount }.sumAmounts()
                )
                entriesForFeeId.forEach { detail ->
                    rows += FeeItem.ChildRow(
                        parentSectionId = sectionId,
                        groupId = feeId,
                        label = "– ${detail.fee_group_type_name}",
                        amount = detail.pending_amount ?: "",
                        feeAmount = detail.fee_amount,
                        discountAmount = detail.discount_amount,
                        paidAmount = detail.paid_amount
                    )
                }
            }

            val title = buildSingleCarryoverTitle(entry)
            val total = entry.amount_to_be_paid ?: entry.pending_carried_over ?: ""

            Section(sectionId, title, total, "⏮\uFE0F", rows, groupedByFeeId.size)
        }
    }

    private fun buildSingleCarryoverTitle(entry: CarryOverFee): String {
        val year = entry.fee_group_type_name?.trim().orEmpty()
        return if (year.isBlank()) "Carryover" else "Carryover ($year)"
    }

    // TRANSPORT — grouped by route_id. Group breakdown passes straight
    // through from the route itself; child (month) breakdown from
    // bus_month_details.
    private fun buildTransportSection(transport: List<TransportFee>?): Section? {
        if (transport.isNullOrEmpty()) return null

        val rows = mutableListOf<FeeItem>()
        transport.forEach { route ->
            rows += FeeItem.GroupRow(
                parentSectionId = "TRANSPORT",
                groupId = route.route_id ?: "",
                label = "${route.route_name} · ${route.route_fee_type_name?.trim() ?: ""}",
                amount = route.pending_amount ?: "",
                feeAmount = route.actual_amount,
                discountAmount = route.discount_given,
                paidAmount = route.paid_amount
            )
            route.bus_month_details.orEmpty().filter { it.is_enabled==true}.forEach { month ->
                rows += FeeItem.ChildRow(
                    parentSectionId = "TRANSPORT",
                    groupId = route.route_id,
                    label = "– ${month.month_name}",
                    amount = month.total_amount ?:"",
                    feeAmount = month.fee_amount,
                    discountAmount = month.discount_amount,
                    paidAmount = month.paid_amount
                )
            }
        }
        val total = transport.map { it.pending_amount }.sumAmounts()
        return Section("TRANSPORT", "Transport", total, "\uD83D\uDE8C", rows, transport.size)
    }

    // HOSTEL — grouped by hostel entry. Group breakdown passes straight
    // through from the hostel entry; child (month) breakdown from
    // month_wise_fee.
    private fun buildHostelSection(hostel: List<HostelFee>?): Section? {
        if (hostel.isNullOrEmpty()) return null

        val rows = mutableListOf<FeeItem>()
        hostel.forEachIndexed { index, h ->
            val groupId = "${h.hostel_name}_${h.room_no}_${h.bed_no}_$index"
            rows += FeeItem.GroupRow(
                parentSectionId = "HOSTEL",
                groupId = groupId,
                label = "${h.hostel_name} (Room ${h.room_no}, Bed ${h.bed_no})",
                amount = h.pending_amount ?: "",
                feeAmount = h.actual_amount,
                discountAmount = h.discount_amount,
                paidAmount = h.paid_amount
            )
            h.month_wise_fee.orEmpty().filter { it.is_enabled==true}.forEach { month ->
                rows += FeeItem.ChildRow(
                    parentSectionId = "HOSTEL",
                    groupId = groupId,
                    label = "– ${month.month_name}",
                    amount = month.pending_amount ?: "",
                    feeAmount = month.actual_amount,
                    discountAmount = month.discount_amount,
                    paidAmount = month.paid_amount
                )
            }
        }
        val total = hostel.map { it.pending_amount }.sumAmounts()
        return Section("HOSTEL", "Hostel", total, "\uD83C\uDFE0", rows, hostel.size)
    }

    // QUANTITY — flat, no grouping. The Quantity payload has no
    // discount_amount/paid_amount fields at all, so those stay null and the
    // adapter hides the breakdown line for these rows rather than showing a
    // fabricated ₹0.00. feeAmount falls back to amount_to_be_paid.
    private fun buildQuantitySection(quantity: List<Quantity>?): Section? {
        if (quantity.isNullOrEmpty()) return null

        val rows = quantity.map { q ->
            FeeItem.ChildRow(
                parentSectionId = "QUANTITY",
                groupId = null,
                label = q.fee_name ?: "",
                amount = q.amount_to_be_paid ?: "",
                feeAmount = q.amount_to_be_paid,
                discountAmount = null,
                paidAmount = null
            )
        }
        val total = quantity.map { it.amount_to_be_paid }.sumAmounts()
        return Section("QUANTITY", "Quantity Fees", total, "\uD83D\uDD22", rows, rows.size)
    }

    // ---- helper for combining several "₹1,234.00"-style amounts ----
    // Currency symbol is read from the source strings, never hardcoded.

    private fun List<String?>.sumAmounts(): String {

        val samples = filterNotNull()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (samples.isEmpty()) return ""

        // Get the currency/prefix from the FIRST amount
        val currency = samples.first()
            .takeWhile { !it.isDigit() && it != '-' }
            .trim()

        // Calculate all amounts accurately
        val total = samples.fold(BigDecimal.ZERO) { sum, amount ->

            val numericValue = amount
                .replace(",", "")
                .filter { it.isDigit() || it == '.' || it == '-' }

            val value = numericValue.toBigDecimalOrNull()
                ?: BigDecimal.ZERO

            sum + value
        }

        // Keep decimal values without unnecessarily adding .00
        val formatter = DecimalFormat("#,##0.############################")

        return currency + formatter.format(total.stripTrailingZeros())
    }
}
//Before
//
//package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProof.FeeBreakDownCustom
//
//import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.OthersModel.GenericFee
//import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofDataModel.PaymentProofFeeDetails
//import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.CarryOverModel.CarryOverFee
//import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.CarryOverModel.CarryOverFeeList
//import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.HostelModel.HostelFee
//import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.Quantity
//import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.TermModel.Term
//import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.TransportModel.TransportFee
//import com.vs.schoolmessenger.R
//
//
///**
// * Internal shape: one section = header info + its flattened rows (GroupRow/ChildRow),
// * already in display order. Building this once per rebuild keeps buildFlatList() trivial.
// */
//private data class Section(
//    val id: String,
//    val title: String,
//    val totalAmount: String,
//    val iconRes: String,
//    val rows: List<FeeItem>,       // GroupRow / ChildRow only — header is added separately
//    val childCountForBadge: Int    // used on the header if you show a "6 items" style badge
//)
//
//object FeeItemMapper {
//
//    fun buildFlatList(feeDetails: PaymentProofFeeDetails, expandedSectionId: String?): List<FeeItem> {
//        val sections = buildSections(feeDetails)
//        val result = mutableListOf<FeeItem>()
//
//        sections.forEach { section ->
//            val isExpanded = section.id == expandedSectionId
//            result += FeeItem.SectionHeader(
//                sectionId = section.id,
//                title = section.title,
//                totalAmount = section.totalAmount,
//                iconRes = section.iconRes,
//                isExpanded = isExpanded,
//                childCount = section.childCountForBadge
//            )
//            if (isExpanded) {
//                result += section.rows
//            }
//        }
//        return result
//    }
//
//    private fun buildSections(fd: PaymentProofFeeDetails): List<Section> {
//        val sections = mutableListOf<Section>()
//
//        buildTermSection(fd.term)?.let { sections += it }
//        buildOthersSection(fd.others)?.let { sections += it }
//        buildCarryoverSection(fd.carryover)?.let { sections += it }
//        buildTransportSection(fd.transport)?.let { sections += it }
//        buildHostelSection(fd.hostel)?.let { sections += it }
//        buildQuantitySection(fd.quantity)?.let { sections += it }
//
//        return sections
//    }
//
//    // ---------------------------------------------------------------------
//    // TERM — flat, no grouping. Each row is "<term name> - <fee name>".
//    // ---------------------------------------------------------------------
//    private fun buildTermSection(terms: List<Term>?): Section? {
//        if (terms.isNullOrEmpty()) return null
//
//        val rows = terms.flatMap { term ->
//            term.fees_details.orEmpty().map { fee ->
//                FeeItem.ChildRow(
//                    parentSectionId = "TERM",
//                    groupId = null,
//                    label = "${term.term_name} - ${fee.fee_name}",
//                    amount = fee.amount_to_be_paid?:""
//                )
//            }
//        }
//        val total = rows.sumOf { it.amount.toRupeeDouble() }.toRupeeString()
//        return Section("TERM", "Term Fees", total, "\uD83D\uDCDA", rows, rows.size)
//    }
//
//    // ---------------------------------------------------------------------
//    // OTHERS — grouped by fee_id, same shape as carryover, in case the same
//    // fee_id ever appears more than once. Group label = fee_name, children
//    // come from month_details.
//    // ---------------------------------------------------------------------
//    private fun buildOthersSection(others: List<GenericFee>?): Section? {
//        if (others.isNullOrEmpty()) return null
//
//        val groupedByFeeId: Map<String, List<GenericFee>> = others.groupBy { it.fee_id?:"" }
//
//        val rows = mutableListOf<FeeItem>()
//        groupedByFeeId.forEach { (feeId, entriesForFeeId) ->
//            val groupLabel = entriesForFeeId.first().fee_name?:""
//            val groupTotal = entriesForFeeId.sumOf { it.amount_to_be_paid!!.toRupeeDouble() }
//            rows += FeeItem.GroupRow(
//                parentSectionId = "OTHERS",
//                groupId = feeId,
//                label = groupLabel,
//                amount = groupTotal.toRupeeString()
//            )
//            entriesForFeeId.forEach { entry ->
//                entry.month_details.orEmpty().forEach { month ->
//                    rows += FeeItem.ChildRow(
//                        parentSectionId = "OTHERS",
//                        groupId = feeId,
//                        label = "– ${month.month_name}",
//                        amount = month.amount_to_be_paid?:""
//                    )
//                }
//            }
//        }
//        val total = others.sumOf { it.amount_to_be_paid!!.toRupeeDouble() }.toRupeeString()
//        val groupCount = groupedByFeeId.size
//        return Section("OTHERS", "Other Fees", total, "\uD83D\uDCC4", rows, groupCount)
//    }
//
//    // ---------------------------------------------------------------------
//    // CARRYOVER — this is the one you asked about. Group by fee_id: same
//    // fee_id across multiple entries (e.g. "Arrear fees" appearing once per
//    // term) collapses into ONE bold GroupRow using fee_name as the label,
//    // with each entry's fee_group_type_name + pending_amount as an indented
//    // ChildRow underneath.
//    // ---------------------------------------------------------------------
//    private fun buildCarryoverSection(carryover: List<CarryOverFee>?): Section? {
//        if (carryover.isNullOrEmpty()) return null
//
//        // Flatten every carry_over_fee list across all carryover entries first,
//        // THEN group by fee_id — a fee_id can repeat across different carryover
//        // parents too, not just within one.
//        val allDetails: List<CarryOverFeeList> = carryover.flatMap { it.carry_over_fee.orEmpty() }
//        val groupedByFeeId: Map<String, List<CarryOverFeeList>> = allDetails.groupBy { it.fee_id?:"" }
//
//        val rows = mutableListOf<FeeItem>()
//        groupedByFeeId.forEach { (feeId, entriesForFeeId) ->
//            val groupLabel = entriesForFeeId.first().fee_name?:""            // e.g. "Arrear fees"
//            val groupTotal = entriesForFeeId.sumOf { it.pending_amount!!.toRupeeDouble() }
//            rows += FeeItem.GroupRow(
//                parentSectionId = "CARRYOVER",
//                groupId = feeId,
//                label = groupLabel,
//                amount = groupTotal.toRupeeString()
//            )
//            entriesForFeeId.forEach { detail ->
//                rows += FeeItem.ChildRow(
//                    parentSectionId = "CARRYOVER",
//                    groupId = feeId,
//                    label = "– ${detail.fee_group_type_name}",             // e.g. "– Term 2"
//                    amount = detail.pending_amount?:""
//                )
//            }
//        }
//
//        val total = carryover.sumOf { it.amount_to_be_paid!!.toRupeeDouble() }.toRupeeString()
//        val groupCount = groupedByFeeId.size
//        val title = buildCarryoverTitle(carryover)
//        return Section("CARRYOVER", title, total, "⏮\uFE0F", rows, groupCount)
//    }
//
//    /**
//     * "Carryover (2025-2026)" — the year in parentheses is fee_group_type_name
//     * from the response, not a hardcoded string. Multiple carryover entries could
//     * in theory carry different fee_group_type_name values, so distinct non-blank
//     * ones are collected rather than assuming there's exactly one.
//     */
//    private fun buildCarryoverTitle(carryover: List<CarryOverFee>): String {
//        val years = carryover
//            .mapNotNull { it.fee_group_type_name?.trim() }
//            .filter { it.isNotBlank() }
//            .distinct()
//
//        return if (years.isEmpty()) "Carryover" else "Carryover (${years.joinToString(", ")})"
//    }
//
//    // ---------------------------------------------------------------------
//    // TRANSPORT — grouped by route_id. Group label = "route · fee type",
//    // children = bus_month_details.
//    // ---------------------------------------------------------------------
//    private fun buildTransportSection(transport: List<TransportFee>?): Section? {
//        if (transport.isNullOrEmpty()) return null
//
//        val rows = mutableListOf<FeeItem>()
//        transport.forEach { route ->
//            rows += FeeItem.GroupRow(
//                parentSectionId = "TRANSPORT",
//                groupId = route.route_id?:"",
//                label = "${route.route_name} · ${route.route_fee_type_name?:"".trim()}",
//                amount = route.pending_amount?:""
//            )
//            route.bus_month_details.orEmpty().forEach { month ->
//                rows += FeeItem.ChildRow(
//                    parentSectionId = "TRANSPORT",
//                    groupId = route.route_id,
//                    label = "– ${month.month_name}",
//                    amount = month.pending ?: month.amount_to_be_paid?:""
//                )
//            }
//        }
//        val total = transport.sumOf { it.pending_amount!!.toRupeeDouble() }.toRupeeString()
//        return Section("TRANSPORT", "Transport", total, "\uD83D\uDE8C", rows, transport.size)
//    }
//
//    // ---------------------------------------------------------------------
//    // HOSTEL — grouped by hostel entry (room + bed makes it unique). Group
//    // label = "hostel (Room X, Bed Y)", children = month_wise_fee.
//    // ---------------------------------------------------------------------
//    private fun buildHostelSection(hostel: List<HostelFee>?): Section? {
//        if (hostel.isNullOrEmpty()) return null
//
//        val rows = mutableListOf<FeeItem>()
//        hostel.forEachIndexed { index, h ->
//            val groupId = "${h.hostel_name}_${h.room_no}_${h.bed_no}_$index"
//            rows += FeeItem.GroupRow(
//                parentSectionId = "HOSTEL",
//                groupId = groupId,
//                label = "${h.hostel_name} (Room ${h.room_no}, Bed ${h.bed_no})",
//                amount = h.pending_amount?:""
//            )
//            h.month_wise_fee.orEmpty().forEach { month ->
//                rows += FeeItem.ChildRow(
//                    parentSectionId = "HOSTEL",
//                    groupId = groupId,
//                    label = "– ${month.month_name}",
//                    amount = month.pending?:""
//                )
//            }
//        }
//        val total = hostel.sumOf { it.pending_amount!!.toRupeeDouble() }.toRupeeString()
//        return Section("HOSTEL", "Hostel", total, "\uD83C\uDFE0", rows, hostel.size)
//    }
//
//    // ---------------------------------------------------------------------
//    // QUANTITY — flat, no grouping (matches the screenshot: two plain rows,
//    // no bold sub-header, no dash prefix).
//    // ---------------------------------------------------------------------
//    private fun buildQuantitySection(quantity: List<Quantity>?): Section? {
//        if (quantity.isNullOrEmpty()) return null
//
//        val rows = quantity.map { q ->
//            FeeItem.ChildRow(
//                parentSectionId = "QUANTITY",
//                groupId = null,
//                label = q.fee_name?:"",
//                amount = q.amount_to_be_paid?:""
//            )
//        }
//        val total = quantity.sumOf { it.amount_to_be_paid!!.toRupeeDouble() }.toRupeeString()
//        return Section("QUANTITY", "Quantity Fees", total, "\uD83D\uDD22", rows, rows.size)
//    }
//
//    // ---- helpers for "₹1,234.00" style strings ----
//
//    private fun String.toRupeeDouble(): Double =
//        this.replace("₹", "").replace(",", "").trim().toDoubleOrNull() ?: 0.0
//
//    private fun Double.toRupeeString(): String =
//        "₹" + "%,.2f".format(this)
//}