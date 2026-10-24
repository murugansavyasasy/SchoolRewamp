package com.vs.schoolmessenger.School.StudentReport.StudentFeeDetails.ModelConvert

import android.content.Context
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.StudentReport.StudentFeeDetails.CardPosition
import com.vs.schoolmessenger.School.StudentReport.StudentFeeDetails.FeeChild
import com.vs.schoolmessenger.School.StudentReport.StudentFeeDetails.FeeListItem
import com.vs.schoolmessenger.School.StudentReport.StudentFeeDetails.FeeStatus
import com.vs.schoolmessenger.School.StudentReport.StudentFeeDetails.Model.*
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Converts FeeDetailsData (API model) -> flat list of FeeListItem for the RecyclerView.
 *
 *  - Every user-visible text comes from strings.xml through [Context]
 *  - Currency is never hardcoded: it is detected from the API amounts (see MoneyFormatter)
 *  - All lists / objects from the API may be null: nothing here uses `!!`
 *
 * Change text / ordering / which sections appear here.
 */
object FeeUiMapper {

    // false = every card starts collapsed
    private const val EXPAND_BY_DEFAULT = false

    fun build(context: Context, d: FeeDetailsData): List<FeeListItem> =
        Builder(context, MoneyFormatter.detect(collectSamples(d))).build(d)

    /** Every amount string we have - used only to learn the currency style. */
    private fun collectSamples(d: FeeDetailsData): List<String?> = buildList {
        add(d.summary?.totalAmount)
        add(d.summary?.totalPaid)
        add(d.summary?.totalPending)
        add(d.summary?.totalDiscount)
        d.termFees.orEmpty().forEach { add(it.actualAmount); add(it.paidAmount); add(it.pendingAmount) }
        d.otherFees.orEmpty().forEach { add(it.actualAmount); add(it.paidAmount); add(it.pendingAmount) }
        d.transportFee.orEmpty().forEach { add(it.actualAmount); add(it.paidAmount); add(it.pendingAmount) }
        d.hostelFee.orEmpty().forEach { add(it.actualAmount); add(it.paidAmount); add(it.pendingAmount) }
        d.carryoverFees.orEmpty().forEach { add(it.actualAmount); add(it.paidAmount); add(it.pendingAmount) }
    }

    /** A card together with its exact pending value, so section totals are summed from numbers, not text. */
    private class Row(val card: FeeListItem.Card, val pending: BigDecimal)

    private class Builder(private val ctx: Context, private val money: MoneyFormatter) {

        private fun str(resId: Int, vararg args: Any) = ctx.getString(resId, *args)
        private fun num(raw: String?) = MoneyFormatter.parse(raw)
        // API amounts are shown exactly as the API sent them; only a blank value falls back to a formatted zero
        private fun fmt(raw: String?): String =
            raw?.trim()?.takeIf { it.isNotEmpty() } ?: money.format(BigDecimal.ZERO)
        private fun orDash(s: String?) = s?.takeIf { it.isNotBlank() } ?: str(R.string.fee_no_value)

        fun build(d: FeeDetailsData): List<FeeListItem> {
            val list = mutableListOf<FeeListItem>()

            // ---- Summary ----
            val total = num(d.summary?.totalAmount)
            val paid = num(d.summary?.totalPaid)
            // keep high precision here; rounding for display is done in the adapter
            val percent = if (total.signum() > 0)
                paid.multiply(BigDecimal(100)).divide(total, 6, RoundingMode.DOWN)
                    .max(BigDecimal.ZERO).min(BigDecimal(100))
            else BigDecimal.ZERO

            list += FeeListItem.Summary(
                total = fmt(d.summary?.totalAmount),
                paid = fmt(d.summary?.totalPaid),
                pending = fmt(d.summary?.totalPending),
                discount = fmt(d.summary?.totalDiscount),
                percent = percent
            )

            // ---- Term fees ----
            addSection(list, str(R.string.fee_term_fees), d.termFees.orEmpty().map {
                row(
                    title = orDash(it.feeName), subtitle = it.termName,
                    fee = it.actualAmount, paid = it.paidAmount,
                    discount = it.discountAmount, pending = it.pendingAmount,
                    children = payments(it.paidDetails)
                )
            })

            // ---- Other fees ----
            addSection(list, str(R.string.fee_other_fees), d.otherFees.orEmpty().map {
                row(
                    title = orDash(it.feeName), subtitle = null,
                    fee = it.actualAmount, paid = it.paidAmount,
                    discount = it.discountAmount, pending = it.pendingAmount,
                    children = payments(it.paidDetails)
                )
            })

            // ---- Transport fees ----
            addSection(list, str(R.string.fee_transport_fees), d.transportFee.orEmpty().map {
                row(
                    title = str(R.string.fee_transport), subtitle = null,
                    fee = it.actualAmount, paid = it.paidAmount,
                    discount = it.discountAmount, pending = it.pendingAmount,
                    children = payments(it.paidDetails)
                )
            })

            // ---- Hostel fees ----
            addSection(list, str(R.string.fee_hostel_fees), d.hostelFee.orEmpty().map {
                row(
                    title = orDash(it.hostelName),
                    subtitle = str(R.string.fee_room_bed, orDash(it.roomNo), orDash(it.bedNo)),
                    fee = it.actualAmount, paid = it.paidAmount,
                    discount = it.discountAmount, pending = it.pendingAmount,
                    children = payments(it.paidDetails)
                )
            })

            // ---- Carryover fees ----
            addSection(list, str(R.string.fee_carryover_fees), d.carryoverFees.orEmpty().map { c ->
                row(
                    title = orDash(c.feeType), subtitle = c.yearName,
                    fee = c.actualAmount, paid = c.paidAmount,
                    discount = c.discountAmount, pending = c.pendingAmount,
                    children = c.breakDown.orEmpty().map { b ->
                        val name = orDash(b.feeName)
                        val group = b.feeGroupTypeName
                        val title = if (group.isNullOrBlank() || name.equals(group, true)) name
                        else str(R.string.fee_name_group, name, group)
                        FeeChild(
                            title = title,
                            subtitle = str(
                                R.string.fee_breakdown_details,
                                fmt(b.feeAmount), fmt(b.paidAmount), fmt(b.pendingAmount)
                            )
                        )
                    }
                )
            })

            //Right now the backend is not giving the Quantity fees
            // Later if added means try to add the design part - add another addSection(...) here when needed.

            return list
        }

        private fun payments(details: List<FeePaidDetail>?): List<FeeChild> =
            details.orEmpty().mapIndexed { i, p ->
                val mode = p.paymentMode?.takeIf { it.isNotBlank() }
                val amount = fmt(p.paidAmount)
                FeeChild(
                    title = str(R.string.fee_payment_title, i + 1, orDash(p.paidDate)),
                    subtitle = if (mode != null) str(R.string.fee_amount_with_mode, amount, mode) else amount
                )
            }

        private fun row(
            title: String, subtitle: String?,
            fee: String?, paid: String?, discount: String?, pending: String?,
            children: List<FeeChild>
        ): Row {
            val paidValue = num(paid)
            val pendingValue = num(pending)
            val card = FeeListItem.Card(
                title = title,
                subtitle = subtitle?.takeIf { it.isNotBlank() },
                details = str(R.string.fee_details_1, fmt(fee), fmt(paid), fmt(discount)),
                pending = fmt(pending),
                status = when {
                    pendingValue.signum() <= 0 -> FeeStatus.PAID
                    paidValue.signum() <= 0 -> FeeStatus.PENDING
                    else -> FeeStatus.PARTIAL
                },
                children = children,
                expanded = EXPAND_BY_DEFAULT && children.isNotEmpty(),
                position = CardPosition.SINGLE
            )
            return Row(card, pendingValue)
        }

        private fun addSection(out: MutableList<FeeListItem>, title: String, rows: List<Row>) {
            if (rows.isEmpty()) return
            val pendingSum = rows.fold(BigDecimal.ZERO) { acc, r -> acc + r.pending }
            out += FeeListItem.SectionHeader(title, money.format(pendingSum))
            rows.forEachIndexed { i, r ->
                val pos = when {
                    rows.size == 1 -> CardPosition.SINGLE
                    i == 0 -> CardPosition.FIRST
                    i == rows.lastIndex -> CardPosition.LAST
                    else -> CardPosition.MIDDLE
                }
                out += r.card.copy(position = pos)
            }
        }
    }
}