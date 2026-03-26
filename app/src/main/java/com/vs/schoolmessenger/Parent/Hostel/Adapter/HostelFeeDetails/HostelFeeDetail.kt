package com.vs.schoolmessenger.Parent.Hostel.Adapter.HostelFeeDetails

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard.FeeDetails
import com.vs.schoolmessenger.R
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

                txtInfo.text =
                    "Hostel: ${item.hostel_details.hostel_name} | Room: ${item.hostel_details.room_no} | Bed: ${item.hostel_details.bed_no}"

                txtTotalValue.text = formatAmount(item.summary.total_amount)
                txtPaidValue.text = formatAmount(item.summary.paid_amount)
                txtPendingValue.text = formatAmount(item.summary.pending_amount)

                txtStatus.text = item.summary.status

                if (item.summary.status.equals("PENDING", true)) {
                    txtStatus.setTextColor(
                        ContextCompat.getColor(context, R.color.orange)
                    )
                    txtStatus.setBackgroundResource(R.drawable.circle_background)
                } else {
                    txtStatus.setTextColor(
                        ContextCompat.getColor(context, R.color.green)
                    )
                    txtStatus.setBackgroundResource(R.drawable.circle_background)
                }

                btnPay.text =
                    "Pay ${formatAmount(item.summary.pending_amount)} Now"
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