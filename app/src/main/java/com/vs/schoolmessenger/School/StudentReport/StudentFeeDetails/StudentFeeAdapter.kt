package com.vs.schoolmessenger.School.StudentReport.StudentFeeDetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R

/**
 * One adapter, three view types: Summary, SectionHeader, Card.
 * Card children (payments / breakdown) are inflated into a container inside the card,
 * so expand/collapse is just a visibility toggle + notifyItemChanged.
 */
class StudentFeeAdapter(
    private val items: List<FeeListItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        const val TYPE_SUMMARY = 0
        const val TYPE_HEADER = 1
        const val TYPE_CARD = 2
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is FeeListItem.Summary -> TYPE_SUMMARY
        is FeeListItem.SectionHeader -> TYPE_HEADER
        is FeeListItem.Card -> TYPE_CARD
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SUMMARY -> SummaryVH(inflater.inflate(R.layout.item_fee_summary, parent, false))
            TYPE_HEADER -> HeaderVH(inflater.inflate(R.layout.item_fee_section_header, parent, false))
            else -> CardVH(inflater.inflate(R.layout.item_fee_card, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SummaryVH -> holder.bind(items[position] as FeeListItem.Summary)
            is HeaderVH -> holder.bind(items[position] as FeeListItem.SectionHeader)
            is CardVH -> holder.bind(items[position] as FeeListItem.Card)
        }
    }

    // ---------------- Summary ----------------
    inner class SummaryVH(v: View) : RecyclerView.ViewHolder(v) {
        private val total: TextView = v.findViewById(R.id.lblTotalFee)
        private val paid: TextView = v.findViewById(R.id.lblPaidFee)
        private val pending: TextView = v.findViewById(R.id.lblPendingFee)
        private val discount: TextView = v.findViewById(R.id.lblDiscountFee)
        private val progress: ProgressBar = v.findViewById(R.id.proPaid)
        private val progressText: TextView = v.findViewById(R.id.lblPaidProgress)

        fun bind(d: FeeListItem.Summary) {
            total.text = d.total
            paid.text = d.paid
            pending.text = d.pending
            discount.text = d.discount
            val percent = d.percent   // exact value, 0..100, works for any amounts

            // Bar: 1000 steps = finer than a pixel, so it is just drawing resolution.
            // Any paid amount > 0 always shows at least a small sliver.
            progress.max = 1000
            val steps = percent.multiply(java.math.BigDecimal(10)).toInt()
            progress.progress = if (percent.signum() > 0 && steps == 0) 1 else steps

            // Text: rounded DOWN so it never shows 100% unless fully paid; tiny values show "<0.1"
            val shown = if (percent.signum() > 0 && percent < java.math.BigDecimal("0.1")) {
                "<0.1"
            } else {
                percent.setScale(1, java.math.RoundingMode.DOWN).stripTrailingZeros().toPlainString()
            }
            progressText.text = itemView.context.getString(
                R.string.fee_paid_progress, d.paid, d.total, shown
            )
        }
    }

    // ---------------- Section header ----------------
    inner class HeaderVH(v: View) : RecyclerView.ViewHolder(v) {
        private val title: TextView = v.findViewById(R.id.lblSectionTitle)

        fun bind(d: FeeListItem.SectionHeader) {
            title.text = itemView.context.getString(R.string.fee_section_title, d.title, d.pending)
        }
    }

    // ---------------- Card ----------------
    inner class CardVH(v: View) : RecyclerView.ViewHolder(v) {
        private val root: View = v.findViewById(R.id.lytFeeCardRoot)
        private val divider: View = v.findViewById(R.id.viewDivider)
        private val header: View = v.findViewById(R.id.lytFeeHeader)
        private val title: TextView = v.findViewById(R.id.lblFeeTitle)
        private val subtitle: TextView = v.findViewById(R.id.lblFeeSubtitle)
        private val details: TextView = v.findViewById(R.id.lblFeeDetails)
        private val status: TextView = v.findViewById(R.id.lblFeeStatus)
        private val pending: TextView = v.findViewById(R.id.lblFeePending)
        private val arrow: ImageView = v.findViewById(R.id.imgArrow)
        private val childContainer: LinearLayout = v.findViewById(R.id.lytChildren)

        fun bind(d: FeeListItem.Card) {
            val ctx = itemView.context

            // card shape by position inside section
            root.setBackgroundResource(
                when (d.position) {
                    CardPosition.SINGLE -> R.drawable.bg_fee_card_single
                    CardPosition.FIRST -> R.drawable.bg_fee_card_top
                    CardPosition.MIDDLE -> R.drawable.bg_fee_card_middle
                    CardPosition.LAST -> R.drawable.bg_fee_card_bottom
                }
            )
            divider.visibility =
                if (d.position == CardPosition.MIDDLE || d.position == CardPosition.LAST) View.VISIBLE else View.GONE

            title.text = d.title
            subtitle.visibility = if (d.subtitle.isNullOrBlank()) View.GONE else View.VISIBLE
            subtitle.text = d.subtitle
            details.text = d.details
            pending.text = d.pending

            // status badge + amount colour
            // label, text colour, badge background - one place to change per status
            val (label, color, background) = when (d.status) {
                FeeStatus.PAID -> Triple(R.string.fee_status_paid, R.color.fee_green, R.drawable.bg_fee_badge_paid)
                FeeStatus.PARTIAL -> Triple(R.string.fee_status_partial, R.color.fee_blue, R.drawable.bg_fee_badge_partial)
                FeeStatus.PENDING -> Triple(R.string.fee_status_pending, R.color.fee_orange, R.drawable.bg_fee_badge_pending)
            }
            status.text = ctx.getString(label)
            status.setTextColor(ContextCompat.getColor(ctx, color))
            status.setBackgroundResource(background)
            pending.setTextColor(ContextCompat.getColor(ctx, if (d.status == FeeStatus.PAID) R.color.fee_green else R.color.fee_orange))

            // expand / collapse
            val expandable = d.children.isNotEmpty()
            arrow.visibility = if (expandable) View.VISIBLE else View.GONE
            childContainer.removeAllViews()

            if (expandable && d.expanded) {
                val inflater = LayoutInflater.from(ctx)
                d.children.forEach { child ->
                    val row = inflater.inflate(R.layout.item_fee_child_row, childContainer, false)
                    row.findViewById<TextView>(R.id.lblChildTitle).text = child.title
                    row.findViewById<TextView>(R.id.lblChildSubtitle).text = child.subtitle
                    childContainer.addView(row)
                }
                childContainer.visibility = View.VISIBLE
                arrow.rotation = 0f      // pointing up
            } else {
                childContainer.visibility = View.GONE
                arrow.rotation = 180f    // pointing down
            }

            header.setOnClickListener {
                if (!expandable) return@setOnClickListener
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                d.expanded = !d.expanded
                notifyItemChanged(pos)
            }
        }
    }
}