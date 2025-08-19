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
    private var itemList: List<InvoiceDetails>?,
    private var listener: InvoiceClickListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.fee_receipt_list_items)
            com.vs.schoolmessenger.Parent.Communication.UnifiedVoiceAdapter.ShimmerViewHolder(
                shimmerView
            )
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.fee_receipt_list_items, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            holder.bind(itemList!![position], listener, position, this)

        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
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