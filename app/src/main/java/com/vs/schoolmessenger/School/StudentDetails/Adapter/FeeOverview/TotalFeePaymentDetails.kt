package com.vs.schoolmessenger.School.StudentDetails.Adapter.FeeOverview

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.StudentDetails.Model.FeeOverview.EntireFeeStructure.FeeOverview
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class TotalFeePaymentDetails(
    private var itemList: List<FeeOverview>?,
    private val context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var expandedPosition = -1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == TYPE_SHIMMER) {

            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.total_fee_payment_details)
            ShimmerViewHolder(shimmerView)

        } else {

            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.total_fee_payment_details, parent, false)

            DataViewHolder(view, context)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList?.size ?: 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is DataViewHolder) {

            itemList?.get(position)?.let { data ->

                holder.bind(data, position, expandedPosition)

                holder.cardHeader.setOnClickListener {

                    val previousExpanded = expandedPosition

                    expandedPosition =
                        if (expandedPosition == position) -1 else position

                    if (previousExpanded != -1) {
                        notifyItemChanged(previousExpanded)
                    }

                    notifyItemChanged(position)
                }
            }
        }
    }

    fun updateData(newList: List<FeeOverview>) {
        itemList = newList
        notifyDataSetChanged()
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val lblFeeName: TextView = itemView.findViewById(R.id.lblFeeName)
        private val lblPending: TextView = itemView.findViewById(R.id.lblPending)
        private val lblTotal: TextView = itemView.findViewById(R.id.lblTotal)
        private val lblDiscount: TextView = itemView.findViewById(R.id.lblDiscount)
        private val lblDueDate: TextView = itemView.findViewById(R.id.lblDueDate)
        private val imgFeeImg: ImageView = itemView.findViewById(R.id.imgFeeImg)
        private val lblStatus: TextView = itemView.findViewById(R.id.lblStatus)

        val downArrowArrow: ImageView = itemView.findViewById(R.id.downArrowArrow)
        val cardHeader: MaterialCardView = itemView.findViewById(R.id.cardHeader)

        private val rcFeeDetails: RecyclerView = itemView.findViewById(R.id.rcFeeDetails)

        fun bind(data: FeeOverview, position: Int, expandedPosition: Int) {

            lblFeeName.text = data.fee_name

            setTwoColorText(
                lblTotal,
                "Paid :",
                data.paid_amount.toString(),
                ContextCompat.getColor(context, R.color.black),
                ContextCompat.getColor(context, R.color.black)
            )

            setTwoColorText(
                lblDiscount,
                "( Disc :",
                data.actual_amount.toString() + ")",
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

            setStatusUI(data.status.toString())
            lblDueDate.visibility= View.GONE


            val dataBreakDown = data.breakDown ?: emptyList()

            if (dataBreakDown.isEmpty()) {
                rcFeeDetails.visibility = View.GONE
                downArrowArrow.visibility = View.GONE
            }
            else {
                downArrowArrow.visibility = View.VISIBLE
                rcFeeDetails.layoutManager = LinearLayoutManager(context)
                rcFeeDetails.isNestedScrollingEnabled = false
                rcFeeDetails.adapter = IndividualFeeDetails(dataBreakDown, context, false)
                val isExpanded = position == expandedPosition
                rcFeeDetails.visibility = if (isExpanded) View.VISIBLE else View.GONE
                downArrowArrow.rotation = if (isExpanded) 180f else 0f
            }
        }

        fun setTwoColorText(
            textView: TextView,
            firstText: String,
            secondText: String,
            firstColor: Int,
            secondColor: Int
        ) {

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

        private fun setStatusUI(status: String) {

            val bgColor: Int
            val textColor: Int
            val iconRes: Int
            val iconTint: Int

            when (status) {

                Constant.paid -> {
                    bgColor = R.color.light_green_1
                    textColor = R.color.green
                    iconRes = R.drawable.tick_icon_2
                    iconTint = R.color.green
                    lblStatus.text = context.getString(R.string.paid)
                }

                Constant.rejected -> {
                    bgColor = R.color.light_red_1
                    textColor = R.color.red
                    iconRes = R.drawable.close_red_color1
                    iconTint = R.color.white
                    lblStatus.text = context.getString(R.string.rejected)
                }

                Constant.pending -> {
                    bgColor = R.color.light_yellow_1
                    textColor = R.color.dark_bg_orange_2
                    iconRes = R.drawable.clock_icon_2
                    iconTint = R.color.dark_bg_orange_2
                    lblStatus.text = context.getString(R.string.pending)
                }

                else -> {
                    bgColor = R.color.white
                    textColor = R.color.black
                    iconRes = R.drawable.alert_icon
                    iconTint = R.color.black
                    lblStatus.text = "Issue"
                }
            }

            applyTintedBackground(lblStatus, R.drawable.rect_radius_15, bgColor)
            lblStatus.setTextColor(ContextCompat.getColor(context, textColor))

            applyTintedBackground(imgFeeImg, R.drawable.circle_bg, bgColor)

            imgFeeImg.setImageResource(iconRes)
            imgFeeImg.setColorFilter(ContextCompat.getColor(context, iconTint))
        }

        fun applyTintedBackground(view: View, drawableRes: Int, colorRes: Int) {

            val context = view.context
            val bgDrawable = ContextCompat.getDrawable(context, drawableRes)

            bgDrawable?.setTint(ContextCompat.getColor(context, colorRes))

            view.background = bgDrawable
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}