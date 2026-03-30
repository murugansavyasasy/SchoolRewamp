package com.vs.schoolmessenger.Parent.Hostel.Adapter.HostelFeeDetails

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.CommonScreens.FilesViewActivity
import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard.FeeDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil
import com.vs.schoolmessenger.databinding.HostelFeeDetailsBinding
import java.text.NumberFormat
import java.util.Locale


class HostelFeeDetail(
    private val context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var fullList: List<FeeDetails> = emptyList()

    override fun getItemViewType(position: Int) =
        if (isLoading) TYPE_SHIMMER else TYPE_DATA

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == TYPE_SHIMMER) {
            val view = ShimmerUtil.wrapWithShimmer(parent, R.layout.hostel_fee_details)
            ShimmerViewHolder(view)
        } else {
            val binding = HostelFeeDetailsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            DataViewHolder(binding)
        }
    }

    override fun getItemCount(): Int =
        if (isLoading) 5 else fullList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(fullList[position])
        }
    }

    fun updateData(newList: List<FeeDetails>) {
        fullList = newList
        isLoading = false
        notifyDataSetChanged()
    }

    inner class DataViewHolder(
        private val binding: HostelFeeDetailsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FeeDetails) {

            with(binding) {

                txtTitle.text = item.fee_name

                val hostel = item.hostel_details.hostel_name
                val room = item.hostel_details.room_no
                val bed = item.hostel_details.bed_no

                val text = "Hostel: $hostel  Room: $room  Bed: $bed"
                val spannable = SpannableString(text)
                spannable.setSpan(
                    ForegroundColorSpan(Color.GRAY),
                    0,
                    "Hostel:".length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                val roomLabelStart = text.indexOf("Room:")
                spannable.setSpan(
                    ForegroundColorSpan(Color.GRAY),
                    roomLabelStart,
                    roomLabelStart + "Room:".length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                val bedLabelStart = text.indexOf("Bed:")
                spannable.setSpan(
                    ForegroundColorSpan(Color.GRAY),
                    bedLabelStart,
                    bedLabelStart + "Bed:".length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                txtInfo.text = spannable

                txtTotalValue.text = formatAmount(item.summary.total_amount)
                txtPaidValue.text = formatAmount(item.summary.paid_amount)
                txtPendingValue.text = formatAmount(item.summary.pending_amount)

                txtStatus.text = item.summary.status

                if (item.summary.status.equals("PENDING", true)) {
                    txtStatus.setTextColor(
                        ContextCompat.getColor(context, R.color.orange)
                    )
                    txtStatus.setBackgroundResource(R.drawable.bg_outline_orange_11)
                    btnPay.visibility= View.VISIBLE
                } else {
                    txtStatus.setTextColor(
                        ContextCompat.getColor(context, R.color.green)
                    )
                    txtStatus.setBackgroundResource(R.drawable.bg_outline_green_11)
                    btnPay.visibility= View.GONE
                }

                btnPay.text =
                    "Pay Now ${formatAmount(item.summary.pending_amount)}"

                btnPay.setOnClickListener {
                    val intent = Intent(context, com.vs.schoolmessenger.Parent.FeeDetails.FeeDetails::class.java)
                    context.startActivity(intent)
                }
            }
        }
    }

    class ShimmerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            ShimmerUtil.startShimmer(view)
        }
    }

    private fun formatAmount(amount: Int): String {
        val format = NumberFormat.getNumberInstance(Locale("en", "IN"))
        return "₹${format.format(amount)}"
    }
}