package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProof.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.ProofDetails
import com.vs.schoolmessenger.R

class TransactionReceiptsAdapter(
    private val context: Context,
    private val items: List<ProofDetails>,
) : RecyclerView.Adapter<TransactionReceiptsAdapter.ReceiptViewHolder>() {

    inner class ReceiptViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rootCard: View = itemView
        val tvPaymentTitle: TextView = itemView.findViewById(R.id.tvPaymentTitle)
        val tvPaymentSubtitle: TextView = itemView.findViewById(R.id.tvPaymentSubtitle)
        val tvFailureMessage: TextView = itemView.findViewById(R.id.tvFailureMessage)
        val viewDiv: View = itemView.findViewById(R.id.viewDiv)
        val rowAmount: View = itemView.findViewById(R.id.rowAmount)
        val rowDate: View = itemView.findViewById(R.id.rowDate)
        val rowTransactionId: View = itemView.findViewById(R.id.rowTransactionId)
        val rowRefNumber: View = itemView.findViewById(R.id.rowRefNumber)
        val rowPayer: View = itemView.findViewById(R.id.rowPayer)
        val rowPayee: View = itemView.findViewById(R.id.rowPayee)
        val rowStatus: View = itemView.findViewById(R.id.rowStatus)
        val rowTime: View = itemView.findViewById(R.id.rowTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.payment_proof_transaction_receipt_item, parent, false)
        return ReceiptViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReceiptViewHolder, position: Int) {
        val item = items[position]

        holder.tvPaymentTitle.text = "${item.upi_provider} • ${item.bank_name}"
        holder.tvPaymentSubtitle.text = item.payment_method
        holder.tvFailureMessage.visibility = if (item.payment_status?.lowercase() == "success") {
            View.GONE
        } else {
            View.VISIBLE
        }
        holder.viewDiv.visibility = if (item.payment_status?.lowercase() == "success") {
            View.GONE
        } else {
            View.VISIBLE
        }
        holder.tvFailureMessage.text = "${context.getString(R.string.failed_reason)} : ${item.failure_reason}"

        bindField(holder.rowAmount, context.getString(R.string.amount), item.paid_amount)
        bindField(holder.rowDate, context.getString(R.string.date), item.receipt_date)
        bindField(holder.rowTransactionId, context.getString(R.string.transaction_id), item.transaction_id)
        bindField(holder.rowRefNumber, context.getString(R.string.ref_number), item.reference_number)
        bindField(holder.rowPayer, context.getString(R.string.payer), item.payer_name)
        bindField(holder.rowPayee, context.getString(R.string.payee), item.payee_name)
        bindField(holder.rowTime, context.getString(R.string.time), item.receipt_time)
        bindField(
            holder.rowStatus,
            context.getString(R.string.payment_status),
            item.payment_status,
            getStatusColor(context, item.payment_status)
        )

    }

    private fun getStatusColor(context: Context, status: String?): Int {
        val colorRes = when (status?.lowercase()) {
            "success" -> R.color.green
            "failed" -> R.color.red
            "unknown" -> R.color.red
            else -> R.color.black
        }
        return ContextCompat.getColor(context, colorRes)
    }
    private fun bindField(row: View, label: String, value: String?) {
        row.findViewById<TextView>(R.id.tvFieldLabel).text = label
        row.findViewById<TextView>(R.id.tvFieldValue).text = value ?: "-"
    }
    private fun bindField(row: View, label: String, value: String?, textColor: Int) {
        row.findViewById<TextView>(R.id.tvFieldLabel).text = label
        row.findViewById<TextView>(R.id.tvFieldValue).apply {
            text = value ?: "-"
            setTextColor(textColor)
        }
    }



    override fun getItemCount(): Int = items.size
}