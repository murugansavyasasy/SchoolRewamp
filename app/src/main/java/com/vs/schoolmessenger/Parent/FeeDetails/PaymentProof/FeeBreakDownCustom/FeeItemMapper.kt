package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProof.FeeBreakDownCustom

import android.content.Context
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.OthersModel.GenericFee
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofDataModel.PaymentProofFeeDetails
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.CarryOverModel.CarryOverFee
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.CarryOverModel.CarryOverFeeList
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.HostelModel.HostelFee
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.Quantity
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.TermModel.Term
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.TransportModel.TransportFee
import com.vs.schoolmessenger.R
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale


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

    fun buildFlatList(context: Context, feeDetails: PaymentProofFeeDetails, expandedSectionId: String?): List<FeeItem> {
        val sections = buildSections(context, feeDetails)
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

    /**
     * The id of whichever section ends up first in display order (Term Fees
     * if present, otherwise Others, otherwise the first Carryover entry, and
     * so on — whatever buildSections() actually produces first for this
     * particular response). Call this once on initial load and use the
     * result to seed expandedSectionId, so the first card starts expanded
     * instead of everything showing collapsed.
     */
    fun firstSectionId(context: Context, feeDetails: PaymentProofFeeDetails): String? =
        buildSections(context, feeDetails).firstOrNull()?.id

    private fun buildSections(context: Context, fd: PaymentProofFeeDetails): List<Section> {
        val sections = mutableListOf<Section>()

        buildTermSection(context, fd.term)?.let { sections += it }
        buildOthersSection(context, fd.others)?.let { sections += it }
        sections += buildCarryoverSections(context, fd.carryover)   // one Section PER entry — never merged
        buildTransportSection(context, fd.transport)?.let { sections += it }
        buildHostelSection(context, fd.hostel)?.let { sections += it }
        buildQuantitySection(context, fd.quantity)?.let { sections += it }

        return sections
    }

    // TERM — flat, no grouping. Breakdown comes straight from each fee entry.
    private fun buildTermSection(context: Context, terms: List<Term>?): Section? {
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
        return Section("TERM", context.getString(R.string.term_fees), total, "\uD83D\uDCDA", rows, rows.size)
    }

    // OTHERS — grouped by fee_id. Group-level breakdown is summed across the
    // entries in that group (no single API field gives that rollup); child
    // (month) breakdown passes straight through from month_details.
    private fun buildOthersSection(context: Context, others: List<GenericFee>?): Section? {
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
        return Section("OTHERS", context.getString(R.string.other_fees), total, "\uD83D\uDCC4", rows, groupedByFeeId.size)
    }

    // CARRYOVER — one Section PER array entry, never merged into one header.
    // Each entry's own amount_to_be_paid is the header total — direct
    // pass-through, never re-summed. Grouping by fee_id happens only within
    // that single entry's own carry_over_fee list. Group breakdown is summed
    // across the term entries in that group; child breakdown passes straight
    // through from each carry_over_fee detail.
    private fun buildCarryoverSections(context: Context, carryover: List<CarryOverFee>?): List<Section> {
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

            val title = buildSingleCarryoverTitle(context, entry)
            val total = entry.amount_to_be_paid ?: entry.pending_carried_over ?: ""

            Section(sectionId, title, total, "⏮\uFE0F", rows, groupedByFeeId.size)
        }
    }

    private fun buildSingleCarryoverTitle(context: Context, entry: CarryOverFee): String {
        val year = entry.fee_group_type_name?.trim().orEmpty()
        return if (year.isBlank()) context.getString(R.string.carryover) else "${context.getString(R.string.carryover)} ($year)"
    }

    // TRANSPORT — grouped by route_id. Group breakdown passes straight
    // through from the route itself; child (month) breakdown from
    // bus_month_details.
    private fun buildTransportSection(context: Context, transport: List<TransportFee>?): Section? {
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
            route.bus_month_details.orEmpty().forEach { month ->
                rows += FeeItem.ChildRow(
                    parentSectionId = "TRANSPORT",
                    groupId = route.route_id,
                    label = "– ${month.month_name}",
                    amount = month.total_amount ?: "",
                    feeAmount = month.fee_amount,
                    discountAmount = month.discount_amount,
                    paidAmount = month.paid_amount
                )
            }
        }
        val total = transport.map { it.pending_amount }.sumAmounts()
        return Section("TRANSPORT", context.getString(R.string.transport), total, "\uD83D\uDE8C", rows, transport.size)
    }

    // HOSTEL — grouped by hostel entry. Group breakdown passes straight
    // through from the hostel entry; child (month) breakdown from
    // month_wise_fee.
    private fun buildHostelSection(context: Context, hostel: List<HostelFee>?): Section? {
        if (hostel.isNullOrEmpty()) return null

        val rows = mutableListOf<FeeItem>()
        hostel.forEachIndexed { index, h ->
            val groupId = "${h.hostel_name}_${h.room_no}_${h.bed_no}_$index"
            rows += FeeItem.GroupRow(
                parentSectionId = "HOSTEL",
                groupId = groupId,
                label = "${h.hostel_name} (${context.getString(R.string.room)} ${h.room_no}, ${context.getString(R.string.Bed)} ${h.bed_no})",
                amount = h.pending_amount ?: "",
                feeAmount = h.actual_amount,
                discountAmount = h.discount_amount,
                paidAmount = h.paid_amount
            )
            h.month_details.orEmpty().forEach { month ->
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
        return Section("HOSTEL", context.getString(R.string.hostel), total, "\uD83C\uDFE0", rows, hostel.size)
    }

    // QUANTITY — flat, no grouping. The Quantity payload has no
    // discount_amount/paid_amount fields at all, so those stay null and the
    // adapter hides the breakdown line for these rows rather than showing a
    // fabricated ₹0.00. feeAmount falls back to amount_to_be_paid.
    private fun buildQuantitySection(context: Context, quantity: List<Quantity>?): Section? {
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
        return Section("QUANTITY", context.getString(R.string.quantity_fees), total, "\uD83D\uDD22", rows, rows.size)
    }

    // ---- helper for combining several "₹1,234.00"-style amounts ----
    // Currency symbol is read from the source strings, never hardcoded.

    private fun List<String?>.sumAmounts(): String {

        val samples = filterNotNull()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (samples.isEmpty()) return ""

        // Get currency/prefix from the FIRST amount
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

        // Force English/normal digits
        val formatter = DecimalFormat(
            "#,##0.############################",
            DecimalFormatSymbols(Locale.ENGLISH)
        )

        return currency + formatter.format(total.stripTrailingZeros())
    }
}

//package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProof.FeeBreakDownCustom
//
//import android.content.Context
//import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.OthersModel.GenericFee
//import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofDataModel.PaymentProofFeeDetails
//import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.CarryOverModel.CarryOverFee
//import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.CarryOverModel.CarryOverFeeList
//import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.HostelModel.HostelFee
//import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.Quantity
//import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.TermModel.Term
//import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.TransportModel.TransportFee
//import com.vs.schoolmessenger.R
//import java.math.BigDecimal
//import java.text.DecimalFormat
//import java.text.DecimalFormatSymbols
//import java.util.Locale
//import kotlin.collections.filter
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
//    val rows: List<FeeItem>,
//    val childCountForBadge: Int
//)
//
//object FeeItemMapper {
//
//    fun buildFlatList(context: Context, feeDetails: PaymentProofFeeDetails, expandedSectionId: String?): List<FeeItem> {
//        val sections = buildSections(context,feeDetails)
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
//    private fun buildSections(context: Context,fd: PaymentProofFeeDetails): List<Section> {
//        val sections = mutableListOf<Section>()
//
//        buildTermSection(context,fd.term)?.let { sections += it }
//        buildOthersSection(context,fd.others)?.let { sections += it }
//        sections += buildCarryoverSections(context,fd.carryover)   // one Section PER entry — never merged
//        buildTransportSection(context,fd.transport)?.let { sections += it }
//        buildHostelSection(context,fd.hostel)?.let { sections += it }
//        buildQuantitySection(context,fd.quantity)?.let { sections += it }
//
//        return sections
//    }
//    // TERM — flat, no grouping. Breakdown comes straight from each fee entry.
//    private fun buildTermSection(    context: Context, terms: List<Term>?): Section? {
//        if (terms.isNullOrEmpty()) return null
//
//        val rows = terms.flatMap { term ->
//            term.fees_details.orEmpty().map { fee ->
//                FeeItem.ChildRow(
//                    parentSectionId = "TERM",
//                    groupId = null,
//                    label = "${term.term_name} - ${fee.fee_name}",
//                    amount = fee.amount_to_be_paid ?: "",
//                    feeAmount = fee.fee_amount,
//                    discountAmount = fee.discount_availed,
//                    paidAmount = fee.actual_paid
//                )
//            }
//        }
//        val total = rows.map { it.amount }.sumAmounts()
//        return Section("TERM",
//            context.getString(R.string.term_fees), total, "\uD83D\uDCDA", rows, rows.size)
//    }
//
//    // OTHERS — grouped by fee_id. Group-level breakdown is summed across the
//    // entries in that group (no single API field gives that rollup); child
//    // (month) breakdown passes straight through from month_details.
//    private fun buildOthersSection(    context: Context,
//                                       others: List<GenericFee>?): Section? {
//        if (others.isNullOrEmpty()) return null
//
//        val groupedByFeeId: Map<String, List<GenericFee>> = others.groupBy { it.fee_id ?: "" }
//
//        val rows = mutableListOf<FeeItem>()
//        groupedByFeeId.forEach { (feeId, entriesForFeeId) ->
//            val groupLabel = entriesForFeeId.first().fee_name ?: ""
//            rows += FeeItem.GroupRow(
//                parentSectionId = "OTHERS",
//                groupId = feeId,
//                label = groupLabel,
//                amount = entriesForFeeId.map { it.amount_to_be_paid }.sumAmounts(),
//                feeAmount = entriesForFeeId.map { it.amount_to_be_paid }.sumAmounts(),
//                discountAmount = entriesForFeeId.map { it.discount_availed }.sumAmounts(),
//                paidAmount = entriesForFeeId.map { it.actual_paid }.sumAmounts()
//            )
//            entriesForFeeId.forEach { entry ->
//                entry.month_details.orEmpty().forEach { month ->
//                    rows += FeeItem.ChildRow(
//                        parentSectionId = "OTHERS",
//                        groupId = feeId,
//                        label = "– ${month.month_name}",
//                        amount = month.amount_to_be_paid ?: "",
//                        feeAmount = month.amount_per_month,
//                        discountAmount = month.discount_amount,
//                        paidAmount = month.paid
//                    )
//                }
//            }
//        }
//        val total = others.map { it.amount_to_be_paid }.sumAmounts()
//        return Section("OTHERS",
//            context.getString(R.string.other_fees), total, "\uD83D\uDCC4", rows, groupedByFeeId.size)
//    }
//
//    // CARRYOVER — one Section PER array entry, never merged into one header.
//    // Each entry's own amount_to_be_paid is the header total — direct
//    // pass-through, never re-summed. Grouping by fee_id happens only within
//    // that single entry's own carry_over_fee list. Group breakdown is summed
//    // across the term entries in that group; child breakdown passes straight
//    // through from each carry_over_fee detail.
//    private fun buildCarryoverSections(    context: Context,
//                                           carryover: List<CarryOverFee>?): List<Section> {
//        if (carryover.isNullOrEmpty()) return emptyList()
//
//        return carryover.mapIndexed { index, entry ->
//            // `id` can repeat across entries (both "2025-2026" and "2024-2025"
//            // may share the same id) — index guarantees each header is distinct.
//            val sectionId = "CARRYOVER_${index}_${entry.id ?: ""}"
//
//            val allDetails: List<CarryOverFeeList> = entry.carry_over_fee.orEmpty()
//            val groupedByFeeId: Map<String, List<CarryOverFeeList>> = allDetails.groupBy { it.fee_id ?: "" }
//
//            val rows = mutableListOf<FeeItem>()
//            groupedByFeeId.forEach { (feeId, entriesForFeeId) ->
//                val groupLabel = entriesForFeeId.first().fee_name ?: ""
//                rows += FeeItem.GroupRow(
//                    parentSectionId = sectionId,
//                    groupId = feeId,
//                    label = groupLabel,
//                    amount = entriesForFeeId.map { it.pending_amount }.sumAmounts(),
//                    feeAmount = entriesForFeeId.map { it.fee_amount }.sumAmounts(),
//                    discountAmount = entriesForFeeId.map { it.discount_amount }.sumAmounts(),
//                    paidAmount = entriesForFeeId.map { it.paid_amount }.sumAmounts()
//                )
//                entriesForFeeId.forEach { detail ->
//                    rows += FeeItem.ChildRow(
//                        parentSectionId = sectionId,
//                        groupId = feeId,
//                        label = "– ${detail.fee_group_type_name}",
//                        amount = detail.pending_amount ?: "",
//                        feeAmount = detail.fee_amount,
//                        discountAmount = detail.discount_amount,
//                        paidAmount = detail.paid_amount
//                    )
//                }
//            }
//
//            val title = buildSingleCarryoverTitle(context,entry)
//            val total = entry.amount_to_be_paid ?: entry.pending_carried_over ?: ""
//
//            Section(sectionId, title, total, "⏮\uFE0F", rows, groupedByFeeId.size)
//        }
//    }
//
//    private fun buildSingleCarryoverTitle(    context: Context,
//                                              entry: CarryOverFee): String {
//        val year = entry.fee_group_type_name?.trim().orEmpty()
//        return if (year.isBlank()) context.getString(R.string.carryover) else "${context.getString(R.string.carryover)} ($year)"
//    }
//
//    // TRANSPORT — grouped by route_id. Group breakdown passes straight
//    // through from the route itself; child (month) breakdown from
//    // bus_month_details.
//    private fun buildTransportSection(    context: Context,
//                                          transport: List<TransportFee>?): Section? {
//        if (transport.isNullOrEmpty()) return null
//
//        val rows = mutableListOf<FeeItem>()
//        transport.forEach { route ->
//            rows += FeeItem.GroupRow(
//                parentSectionId = "TRANSPORT",
//                groupId = route.route_id ?: "",
//                label = "${route.route_name} · ${route.route_fee_type_name?.trim() ?: ""}",
//                amount = route.pending_amount ?: "",
//                feeAmount = route.actual_amount,
//                discountAmount = route.discount_given,
//                paidAmount = route.paid_amount
//            )
//            route.bus_month_details.orEmpty().forEach { month ->
//                rows += FeeItem.ChildRow(
//                    parentSectionId = "TRANSPORT",
//                    groupId = route.route_id,
//                    label = "– ${month.month_name}",
//                    amount = month.total_amount ?:"",
//                    feeAmount = month.fee_amount,
//                    discountAmount = month.discount_amount,
//                    paidAmount = month.paid_amount
//                )
//            }
//        }
//        val total = transport.map { it.pending_amount }.sumAmounts()
//        return Section("TRANSPORT",
//            context.getString(R.string.transport), total, "\uD83D\uDE8C", rows, transport.size)
//    }
//
//    // HOSTEL — grouped by hostel entry. Group breakdown passes straight
//    // through from the hostel entry; child (month) breakdown from
//    // month_wise_fee.
//    private fun buildHostelSection(context: Context,hostel: List<HostelFee>?): Section? {
//        if (hostel.isNullOrEmpty()) return null
//
//        val rows = mutableListOf<FeeItem>()
//        hostel.forEachIndexed { index, h ->
//            val groupId = "${h.hostel_name}_${h.room_no}_${h.bed_no}_$index"
//            rows += FeeItem.GroupRow(
//                parentSectionId = "HOSTEL",
//                groupId = groupId,
//                label = "${h.hostel_name} (${context.getString(R.string.room)} ${h.room_no}, ${context.getString(R.string.Bed)} ${h.bed_no})",
//                amount = h.pending_amount ?: "",
//                feeAmount = h.actual_amount,
//                discountAmount = h.discount_amount,
//                paidAmount = h.paid_amount
//            )
//            h.month_details.orEmpty().forEach { month ->
//                rows += FeeItem.ChildRow(
//                    parentSectionId = "HOSTEL",
//                    groupId = groupId,
//                    label = "– ${month.month_name}",
//                    amount = month.pending_amount ?: "",
//                    feeAmount = month.actual_amount,
//                    discountAmount = month.discount_amount,
//                    paidAmount = month.paid_amount
//                )
//            }
//        }
//        val total = hostel.map { it.pending_amount }.sumAmounts()
//        return Section("HOSTEL", context.getString(R.string.hostel), total, "\uD83C\uDFE0", rows, hostel.size)
//    }
//
//    // QUANTITY — flat, no grouping. The Quantity payload has no
//    // discount_amount/paid_amount fields at all, so those stay null and the
//    // adapter hides the breakdown line for these rows rather than showing a
//    // fabricated ₹0.00. feeAmount falls back to amount_to_be_paid.
//    private fun buildQuantitySection(    context: Context,
//                                         quantity: List<Quantity>?): Section? {
//        if (quantity.isNullOrEmpty()) return null
//
//        val rows = quantity.map { q ->
//            FeeItem.ChildRow(
//                parentSectionId = "QUANTITY",
//                groupId = null,
//                label = q.fee_name ?: "",
//                amount = q.amount_to_be_paid ?: "",
//                feeAmount = q.amount_to_be_paid,
//                discountAmount = null,
//                paidAmount = null
//            )
//        }
//        val total = quantity.map { it.amount_to_be_paid }.sumAmounts()
//        return Section("QUANTITY",
//            context.getString(R.string.quantity_fees), total, "\uD83D\uDD22", rows, rows.size)
//    }
//
//    // ---- helper for combining several "₹1,234.00"-style amounts ----
//    // Currency symbol is read from the source strings, never hardcoded.
//
//    private fun List<String?>.sumAmounts(): String {
//
//        val samples = filterNotNull()
//            .map { it.trim() }
//            .filter { it.isNotEmpty() }
//
//        if (samples.isEmpty()) return ""
//
//        // Get currency/prefix from the FIRST amount
//        val currency = samples.first()
//            .takeWhile { !it.isDigit() && it != '-' }
//            .trim()
//
//        // Calculate all amounts accurately
//        val total = samples.fold(BigDecimal.ZERO) { sum, amount ->
//
//            val numericValue = amount
//                .replace(",", "")
//                .filter { it.isDigit() || it == '.' || it == '-' }
//
//            val value = numericValue.toBigDecimalOrNull()
//                ?: BigDecimal.ZERO
//
//            sum + value
//        }
//
//        // Force English/normal digits
//        val formatter = DecimalFormat(
//            "#,##0.############################",
//            DecimalFormatSymbols(Locale.ENGLISH)
//        )
//
//        return currency + formatter.format(total.stripTrailingZeros())
//    }
//}
