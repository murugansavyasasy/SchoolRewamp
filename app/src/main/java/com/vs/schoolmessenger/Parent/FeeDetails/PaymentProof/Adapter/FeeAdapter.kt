package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProof.Adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProof.FeeBreakDownCustom.FeeItem
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.ProofItemFeeChildBinding
import com.vs.schoolmessenger.databinding.ProofItemFeeGroupBinding
import com.vs.schoolmessenger.databinding.ProofItemFeeHeaderBinding


private const val VIEW_TYPE_HEADER = 0
private const val VIEW_TYPE_GROUP = 1
private const val VIEW_TYPE_CHILD = 2

class FeeAdapter(
    private val onHeaderClicked: (sectionId: String) -> Unit
) : ListAdapter<FeeItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is FeeItem.SectionHeader -> VIEW_TYPE_HEADER
            is FeeItem.GroupRow -> VIEW_TYPE_GROUP
            is FeeItem.ChildRow -> VIEW_TYPE_CHILD
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(
                ProofItemFeeHeaderBinding.inflate(inflater, parent, false), onHeaderClicked
            )
            VIEW_TYPE_GROUP -> GroupViewHolder(
                ProofItemFeeGroupBinding.inflate(inflater, parent, false)
            )
            else -> ChildViewHolder(
                ProofItemFeeChildBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind(getItem(position) as FeeItem.SectionHeader)
            is GroupViewHolder -> holder.bind(getItem(position) as FeeItem.GroupRow)
            is ChildViewHolder -> holder.bind(getItem(position) as FeeItem.ChildRow)
        }
    }

    class HeaderViewHolder(
        private val binding: ProofItemFeeHeaderBinding,
        private val onHeaderClicked: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FeeItem.SectionHeader) {
            binding.icon.text = item.iconRes
            binding.title.text = item.title
            binding.amount.text = item.totalAmount
            binding.arrow.rotation = if (item.isExpanded) 180f else 0f
            binding.arrow.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    binding.root.context,
                    if (item.isExpanded) R.color.PrimaryColor else R.color.black
                )
            )
            binding.root.setOnClickListener { onHeaderClicked(item.sectionId) }
        }
    }

    class GroupViewHolder(
        private val binding: ProofItemFeeGroupBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FeeItem.GroupRow) {
            binding.groupLabel.text = item.label
            binding.groupAmount.text = item.amount
            bindBreakdown(
                container = binding.breakdownRow,
                feeAmountLabel = binding.feeAmountValue,
                discountLabel = binding.discountValue,
                paidLabel = binding.paidValue,
                feeAmount = item.feeAmount,
                discount = item.discountAmount,
                paid = item.paidAmount
            )
//            Right now we no need to show the group header's Fee amount,Discount,Paid amount so that's why goned!
            binding.breakdownRow.visibility= View.GONE
        }
    }

    class ChildViewHolder(
        private val binding: ProofItemFeeChildBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FeeItem.ChildRow) {
            binding.label.text = item.label
            binding.amount.text = item.amount
            bindBreakdown(
                container = binding.breakdownRow,
                feeAmountLabel = binding.feeAmountValue,
                discountLabel = binding.discountValue,
                paidLabel = binding.paidValue,
                feeAmount = item.actualAmount,
                discount = item.pending,
                paid = item.paidAmount
            )
        }
    }

    companion object {

        /**
         * Shared by both GroupViewHolder and ChildViewHolder. If all three
         * values are missing (e.g. Quantity rows, whose API payload has no
         * discount/paid fields), the whole breakdown line is hidden rather
         * than showing a fabricated "₹0.00".
         */
        private fun bindBreakdown(
            container: View,
            feeAmountLabel: android.widget.TextView,
            discountLabel: android.widget.TextView,
            paidLabel: android.widget.TextView,
            feeAmount: String?,
            discount: String?,
            paid: String?
        ) {
            if (feeAmount.isNullOrBlank() && discount.isNullOrBlank() && paid.isNullOrBlank()) {
                container.visibility = View.GONE
                return
            }
            container.visibility = View.VISIBLE
            feeAmountLabel.text = feeAmount ?: "-"
            discountLabel.text = discount ?: "-"
            paidLabel.text = paid ?: "-"
        }

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FeeItem>() {
            override fun areItemsTheSame(old: FeeItem, new: FeeItem): Boolean =
                when {
                    old is FeeItem.SectionHeader && new is FeeItem.SectionHeader ->
                        old.sectionId == new.sectionId
                    old is FeeItem.GroupRow && new is FeeItem.GroupRow ->
                        old.parentSectionId == new.parentSectionId && old.groupId == new.groupId
                    old is FeeItem.ChildRow && new is FeeItem.ChildRow ->
                        old.parentSectionId == new.parentSectionId &&
                                old.groupId == new.groupId &&
                                old.label == new.label
                    else -> false
                }

            override fun areContentsTheSame(old: FeeItem, new: FeeItem): Boolean = old == new
        }
    }
}
