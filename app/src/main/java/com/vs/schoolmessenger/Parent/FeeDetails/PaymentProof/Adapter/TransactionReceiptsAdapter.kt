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

        holder.tvPaymentTitle.text =
            "${displayValue(item.upi_provider)} • ${displayValue(item.bank_name)}"

        holder.tvPaymentSubtitle.text = displayValue(item.payment_method)

        val isSuccess = item.payment_status?.equals("success", ignoreCase = true) == true

        val hasFailureReason = !item.failure_reason.isNullOrBlank()

        val showFailureReason = !isSuccess && hasFailureReason

        holder.tvFailureMessage.visibility =
            if (showFailureReason) View.VISIBLE else View.GONE

        holder.viewDiv.visibility =
            if (showFailureReason) View.VISIBLE else View.GONE

        holder.tvFailureMessage.text =
            "${context.getString(R.string.failed_reason)} : ${displayValue(item.failure_reason)}"

        bindField(
            holder.rowAmount,
            context.getString(R.string.amount),
            displayValue(item.paid_amount)
        )

        bindField(
            holder.rowDate,
            context.getString(R.string.date),
            displayValue(item.receipt_date)
        )

        bindField(
            holder.rowTransactionId,
            context.getString(R.string.transaction_id),
            displayValue(item.transaction_id)
        )

        bindField(
            holder.rowRefNumber,
            context.getString(R.string.ref_number),
            displayValue(item.reference_number)
        )

        bindField(
            holder.rowPayer,
            context.getString(R.string.payer),
            displayValue(item.payer_name)
        )

        bindField(
            holder.rowPayee,
            context.getString(R.string.payee),
            displayValue(item.payee_name)
        )

        bindField(
            holder.rowTime,
            context.getString(R.string.time),
            displayValue(item.receipt_time)
        )

        bindField(
            holder.rowStatus,
            context.getString(R.string.payment_status),
            displayValue(item.payment_status),
            getStatusColor(context, item.payment_status)
        )

    }
    private fun displayValue(value: String?): String {
        return value?.uppercase()?.takeIf { it.isNotBlank() } ?: "-"
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