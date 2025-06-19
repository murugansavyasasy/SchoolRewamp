package com.vs.schoolmessenger.School.DailyCollection

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.DailyCollection.DailyCollectionModel.DailyCollectionDisplayItem


class DcfAdapter(
    private var itemList: List<DailyCollectionDisplayItem>,
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (itemList[position]) {
            is DailyCollectionDisplayItem.Header -> TYPE_HEADER
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_dcf_header, parent, false)
                HeaderViewHolder(view)
            }

            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = itemList[position]
        if (holder is HeaderViewHolder && item is DailyCollectionDisplayItem.Header) {
            holder.bind(item)
        }
    }

    override fun getItemCount(): Int = itemList.size

    fun clearData() {
        itemList = emptyList()
        notifyDataSetChanged()
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val totalLabel: TextView = itemView.findViewById(R.id.total_label)
        private val totalValue: TextView = itemView.findViewById(R.id.total_value)
        private val recyclerView: RecyclerView = itemView.findViewById(R.id.feefootersummary)

        fun bind(item: DailyCollectionDisplayItem.Header) {
            totalLabel.text = item.category
            totalValue.text = item.total

            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = FeeOnlyAdapter(item.feeList)
        }
    }

    inner class FeeOnlyAdapter(private val fees: List<DailyCollectionDisplayItem.Fee>) :
        RecyclerView.Adapter<FeeOnlyAdapter.FeeViewHolder>() {

        inner class FeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val feeType: TextView = itemView.findViewById(R.id.fee_type)
            private val feeAmount: TextView = itemView.findViewById(R.id.fee_amount)

            fun bind(item: DailyCollectionDisplayItem.Fee) {
                feeType.text = item.typeName
                feeAmount.text = item.amount
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeeViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_dcf_fee, parent, false)
            return FeeViewHolder(view)
        }

        override fun onBindViewHolder(holder: FeeViewHolder, position: Int) {
            holder.bind(fees[position])
        }

        override fun getItemCount(): Int = fees.size
    }
}
