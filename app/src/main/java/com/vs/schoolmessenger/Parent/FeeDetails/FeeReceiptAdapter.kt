package com.vs.schoolmessenger.Parent.FeeDetails

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil

class FeeReceiptAdapter(
    private var originalList: List<InvoiceDetails>?,
    private var listener: InvoiceClickListener,
    private var context: Context,
    private var isLoading: Boolean,
    private var filterResultListener: FeeDetails.OnFilterResultListener? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), android.widget.Filterable {

    private var filteredList: List<InvoiceDetails>? = originalList

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.fee_receipt_list_items)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fee_receipt_list_items, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            filteredList?.get(position)?.let {
                holder.bind(it, listener, position, this)
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else filteredList?.size ?: 0
    }


    override fun getFilter(): android.widget.Filter {
        return object : android.widget.Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""

                val results = if (query.isEmpty()) {
                    originalList ?: listOf()
                } else {
                    originalList?.filter {
                        it.lblInvoiceNo.lowercase().contains(query) ||
                                it.lblInvoiceDate.lowercase().contains(query) ||
                                it.lblInvoiceAmount.lowercase().contains(query) ||
                                it.lblInvoiceTime.lowercase().contains(query)
                    } ?: listOf()
                }

                return FilterResults().apply { values = results }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<InvoiceDetails> ?: listOf()
                notifyDataSetChanged()
                filterResultListener?.onFilterResult(filteredList.isNullOrEmpty())
            }
        }
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val lblInvoiceNo: TextView = itemView.findViewById(R.id.lblInvoiceNo)
        private val lblInvoiceDate: TextView = itemView.findViewById(R.id.lblInvoiceDate)
        private val lblInvoiceAmount: TextView = itemView.findViewById(R.id.lblInvoiceAmount)
        private val lblInvoiceTime: TextView = itemView.findViewById(R.id.lblInvoiceTime)
        private val rytView: RelativeLayout = itemView.findViewById(R.id.rytView)

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(
            data: InvoiceDetails,
            listener: InvoiceClickListener,
            position: Int,
            adapter: FeeReceiptAdapter
        ) {
            lblInvoiceNo.text = data.lblInvoiceNo
            lblInvoiceDate.text = data.lblInvoiceDate
            lblInvoiceAmount.text = data.lblInvoiceAmount
            lblInvoiceTime.text = data.lblInvoiceTime

            rytView.setOnClickListener {
                listener.onItemClick(data, this@DataViewHolder)
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
