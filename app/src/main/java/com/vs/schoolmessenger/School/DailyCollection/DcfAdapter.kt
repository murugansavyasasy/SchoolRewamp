package com.vs.schoolmessenger.School.DailyCollection

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R


class DcfAdapter(
    private var itemList: List<DisplayItem>,
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 1
        private const val TYPE_FEE = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (itemList[position]) {
            is DisplayItem.Header -> TYPE_HEADER
            is DisplayItem.Fee -> TYPE_FEE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_dcf_header, parent, false)
                HeaderViewHolder(view)
            }
            TYPE_FEE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_dcf_fee, parent, false)
                FeeViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    fun clearData() {
        itemList = emptyList()
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = itemList[position]
        when {
            holder is HeaderViewHolder && item is DisplayItem.Header -> holder.bind(item)
            holder is FeeViewHolder && item is DisplayItem.Fee -> holder.bind(item)
        }
    }

    override fun getItemCount(): Int = itemList.size

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val totalLabel: TextView = itemView.findViewById(R.id.total_label)
        private val totalValue: TextView = itemView.findViewById(R.id.total_value)

        fun bind(item: DisplayItem.Header) {
            totalLabel.text = item.category
            totalValue.text = item.total
        }
    }

    inner class FeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val feeType: TextView = itemView.findViewById(R.id.fee_type)
        private val feeAmount: TextView = itemView.findViewById(R.id.fee_amount)

        fun bind(item: DisplayItem.Fee) {
            feeType.text = item.typeName
            feeAmount.text = item.amount
        }
    }
}