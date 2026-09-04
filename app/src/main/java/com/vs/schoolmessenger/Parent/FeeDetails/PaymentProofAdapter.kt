package com.vs.schoolmessenger.Parent.FeeDetails

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentDetails
import com.vs.schoolmessenger.databinding.ItemPaymentProofBinding

class PaymentProofAdapter(
    private val items: List<PaymentDetails>,
    private val onImageClick: (String) -> Unit
) : RecyclerView.Adapter<PaymentProofAdapter.ProofViewHolder>() {

    inner class ProofViewHolder(val binding: ItemPaymentProofBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProofViewHolder {
        val binding = ItemPaymentProofBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProofViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ProofViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvAmount.text = item.total_amount
            tvCreatedOn.text = item.created_on
            tvRemarks.text = if (!item.remarks.isNullOrBlank()) "Remarks: ${item.remarks}" else ""

            when (item.is_payment_validated?.lowercase()) {
                "approved" -> {
                    tvStatus.text = "APPROVED"
                    tvStatus.setBackgroundColor(Color.parseColor("#2E7D32"))
                }
                "rejected" -> {
                    tvStatus.text = "REJECTED"
                    tvStatus.setBackgroundColor(Color.parseColor("#C62828"))
                }
                else -> {
                    tvStatus.text = item.is_payment_validated?.uppercase()
                    tvStatus.setBackgroundColor(Color.parseColor("#F9A825"))
                }
            }

            val proofUrl = item.proof_uploaded?.firstOrNull()?.aws_url
            if (proofUrl != null) {
                Glide.with(ivProof.context)
                    .load(proofUrl)
                    .centerCrop()
                    .into(ivProof)
                ivProof.setOnClickListener { onImageClick(proofUrl) }
            }

            val proofDetail = item.proof_details?.firstOrNull()
            tvValidationMessage.text = proofDetail?.let {
                "${it.validation_message ?: ""} (${it.confidence}% confidence)"
            } ?: ""
        }
    }
}