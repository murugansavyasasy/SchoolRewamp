package com.vs.schoolmessenger.School.StudentDetails.Adapter.PaymentHistory

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.StudentDetails.Model.FeeOverview.PaymentHistory.PaymentHistory
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class PaymentHistoryAdapter(
    private var itemList: List<PaymentHistory>?,
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
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.payment_history_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.payment_history_item, parent, false)
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            itemList?.get(position)?.let { holder.bind(it, position) }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }


    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList?.size ?: 0
    }

    fun updateData(newList: List<PaymentHistory>) {
        itemList = newList
        notifyDataSetChanged()
    }


    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val lblFeeName: TextView = itemView.findViewById(R.id.lblFeeName)
        private val lblPaymentDetails: TextView = itemView.findViewById(R.id.lblPaymentDetails)
        private val lblDiscount: TextView = itemView.findViewById(R.id.lblDiscount)
        private val lblTotal: TextView = itemView.findViewById(R.id.lblTotal)
        private val lblPending: TextView = itemView.findViewById(R.id.lblPending)
        private val lblPaidDate: TextView = itemView.findViewById(R.id.lblPaidDate)
        private val lblTransactionNo: TextView = itemView.findViewById(R.id.lblTransactionNo)

        fun bind(data: PaymentHistory, position: Int) {

            lblFeeName.text=data.payment_mode
            lblTransactionNo.text=data.paymentId
            lblPaidDate.text=data.paid_date
            lblPaymentDetails.text=data.payment_mode

            val icon = when (data.payment_mode) {
                "UPI" -> R.drawable.card__payment_icon
                "Cash" -> R.drawable.cash_icon

                else -> R.drawable.tick_icon_2
            }

            lblPaymentDetails.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)

            setTwoColorText(
                lblTotal,
                "Paid",
                data.paid_amount.toString(),
                ContextCompat.getColor(context, R.color.black),
                ContextCompat.getColor(context, R.color.black)
            )

            setTwoColorText(
                lblDiscount,
                "( Disc :",
                data.discount_amount.toString()+")",
                ContextCompat.getColor(context, R.color.light_violet_12),
                ContextCompat.getColor(context, R.color.light_violet_12)
            )

            setTwoColorText(
                lblPending,
                "Pending :",
                data.pending_amount.toString(),
                ContextCompat.getColor(context, R.color.red),
                ContextCompat.getColor(context, R.color.red)
            )


        }



        fun setTwoColorText(textView: TextView, firstText: String, secondText: String, firstColor: Int, secondColor: Int) {

            val fullText = "$firstText $secondText"
            val spannable = SpannableString(fullText)

            spannable.setSpan(
                ForegroundColorSpan(firstColor),
                0,
                firstText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            spannable.setSpan(
                ForegroundColorSpan(secondColor),
                firstText.length + 1,
                fullText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            textView.text = spannable
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)

        }
    }

}