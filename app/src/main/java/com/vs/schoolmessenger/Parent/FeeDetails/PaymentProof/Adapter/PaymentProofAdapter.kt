package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProof.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofDataModel.PaymentDetails
import com.vs.schoolmessenger.R

class PaymentProofAdapter(
    private var fullList: List<PaymentDetails>,
    private val context: Context,
    private val onPaymentClick: (PaymentDetails) -> Unit
) : RecyclerView.Adapter<PaymentProofAdapter.DataViewHolder>() {

    private var filteredList: List<PaymentDetails> = fullList


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DataViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_payment_proof,
                parent,
                false
            )

        return DataViewHolder(view)
    }


    override fun getItemCount(): Int {
        return filteredList.size
    }


    override fun onBindViewHolder(
        holder: DataViewHolder,
        position: Int
    ) {
        holder.bind(filteredList[position])
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<PaymentDetails>) {

        fullList = newList

        filteredList = newList

        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterByStatusAndSearch(
        status: String,
        searchText: String
    ) {

        val query =
            searchText.trim().lowercase()


        filteredList =
            fullList.filter { payment ->

                val statusMatches =
                    status.equals(
                        "all",
                        ignoreCase = true
                    ) ||
                            payment.is_payment_validated
                                ?.trim()
                                ?.equals(
                                    status.trim(),
                                    ignoreCase = true
                                ) == true


                if (!statusMatches) {
                    return@filter false
                }

                if (query.isEmpty()) {
                    return@filter true
                }


                val userAmountMatches =
                    payment.user_enter_amount
                        ?.lowercase()
                        ?.contains(query) == true


                val aiAmountMatches =
                    payment.ai_detected_amount
                        ?.lowercase()
                        ?.contains(query) == true


                val totalAmountMatches =
                    payment.total_amount
                        ?.lowercase()
                        ?.contains(query) == true


                val dateMatches =
                    payment.created_on
                        ?.lowercase()
                        ?.contains(query) == true


                val statusSearchMatches =
                    payment.is_payment_validated
                        ?.lowercase()
                        ?.contains(query) == true


                userAmountMatches ||
                        aiAmountMatches ||
                        totalAmountMatches ||
                        dateMatches ||
                        statusSearchMatches
            }


        notifyDataSetChanged()
    }

    fun getStatusCount(
        status: String
    ): Int {

        return if (
            status.equals(
                "all",
                ignoreCase = true
            )
        ) {

            fullList.size

        } else {

            fullList.count {

                it.is_payment_validated.equals(
                    status,
                    ignoreCase = true
                )
            }
        }
    }


    inner class DataViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val lblAmount: TextView =
            itemView.findViewById(
                R.id.lblAmount
            )


        private val lblDate: TextView =
            itemView.findViewById(
                R.id.lblDate
            )
        private val cardPayment: CardView =
            itemView.findViewById(
                R.id.cardPayment
            )


        private val lblStatus: TextView =
            itemView.findViewById(
                R.id.lblStatus
            )


        private val viewStatusDot: View =
            itemView.findViewById(
                R.id.viewStatusDot
            )


        @SuppressLint("SetTextI18n")
        fun bind(
            data: PaymentDetails
        ) {

            lblAmount.text =
                data.user_enter_amount ?: "0.00"

            val status =
                data.is_payment_validated
                    ?.trim()
                    ?.lowercase()
                    ?: "pending"



            val createdDate =
                data.created_on ?: ""


            cardPayment.setOnClickListener {
                onPaymentClick(data)
            }

            val photoCount =
                data.proof_uploaded?.size ?: 0


            val photosLetter =
                if (photoCount == 1) {
                    context.getString(R.string.attachment)

                } else {

                    context.getString(
                        R.string.attachments
                    )
                }


            lblDate.text =
                if (photoCount > 0) {

                    "$createdDate · $photoCount $photosLetter"

                } else {

                    createdDate
                }


//            if (
//                status.equals(
//                    "pending",
//                    ignoreCase = true
//                )
//            ) {
//
//                lblAmount.setCompoundDrawablesWithIntrinsicBounds(
//                    0,
//                    0,
//                    R.drawable.sparkles_emoji_icon,
//                    0
//                )
//
//                // Space between amount and sparkle
//                lblAmount.compoundDrawablePadding =
//                    dpToPx(4)
//
//            } else {
//
//                lblAmount.setCompoundDrawablesWithIntrinsicBounds(
//                    0,
//                    0,
//                    0,
//                    0
//                )
//
//                lblAmount.compoundDrawablePadding = 0
//            }


            when (status) {
                "approved" -> {

                    lblStatus.text =
                        context.getString(
                            R.string.approved
                        )


                    lblStatus.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.green
                        )
                    )


                    viewStatusDot.background =
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.bg_payment_approved_dot
                        )
                }
                "rejected" -> {

                    lblStatus.text =
                        context.getString(
                            R.string.rejected
                        )


                    lblStatus.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.red
                        )
                    )


                    viewStatusDot.background =
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.bg_payment_rejected_dot
                        )
                }

                else -> {

                    lblStatus.text =
                        context.getString(
                            R.string.pending
                        )


                    lblStatus.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.dark_brown_3
                        )
                    )


                    viewStatusDot.background =
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.bg_payment_pending_dot
                        )
                }
            }
        }

        private fun dpToPx(
            dp: Int
        ): Int {

            return (
                    dp *
                            context.resources
                                .displayMetrics
                                .density
                    ).toInt()
        }
    }
}