package com.vs.schoolmessenger.School.Hostel.Adapter.FeeManagement


import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Hostel.Model.FeeManagement.FeeManagementData

import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class FeeManagementAdapter(
    private var itemList: List<FeeManagementData>?,
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
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.fee_mangement_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fee_mangement_item, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList?.size ?: 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            itemList?.get(position)?.let {
                holder.bind(it, position)
            }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    fun updateData(newList: List<FeeManagementData>) {
        itemList = newList
        notifyDataSetChanged()
    }

    class DataViewHolder(
        itemView: View,
        private val context: Context
    ) : RecyclerView.ViewHolder(itemView) {

        private val lblTotalAmount: TextView = itemView.findViewById(R.id.lblTotalAmount)
        private val lblYear: TextView = itemView.findViewById(R.id.lblYear)
        private val lblFullName: TextView = itemView.findViewById(R.id.lblFullName)
        private val lblDueDateOn: TextView = itemView.findViewById(R.id.lblDueDateOn)
        private val lblOverDue: TextView = itemView.findViewById(R.id.lblOverDue)
        private val lblDue: TextView = itemView.findViewById(R.id.lblDue)
        private val lblRoomNo: TextView = itemView.findViewById(R.id.lblRoomNo)
        private val lblStatus: TextView = itemView.findViewById(R.id.lblStatus)
        private val cardHeader: MaterialCardView = itemView.findViewById(R.id.cardHeader)

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: FeeManagementData, position: Int) {
            lblFullName.text = data.studentName

            lblDueDateOn.text = data.dueDate

            lblTotalAmount.text = "₹${data.amount}"
            lblRoomNo.text = data.roomDetails
            lblYear.text = data.year

            if (data.status == Constant.paid) {
                lblOverDue.visibility= View.GONE

                applyTintedBackground(
                    lblStatus,
                    R.drawable.rect_bg_light_green_present,
                    R.color.very_light_green_3
                )
                lblStatus.text=data.status
                lblStatus.setTextColor(context.getColor(R.color.green))


                cardHeader.setStrokeColor(
                    ContextCompat.getColor(itemView.context, R.color.light_green_four)
                )

                cardHeader.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.light_green_bg)
                )

            }
            else if (data.status == Constant.pending) {

                lblStatus.text=context.getString(R.string.pending)
                lblStatus.setTextColor(context.getColor(R.color.dark_brown_3))

                applyTintedBackground(
                    lblStatus,
                    R.drawable.rect_bg_light_green_present,
                    R.color.very_light_orange_3
                )

                cardHeader.setStrokeColor(
                    ContextCompat.getColor(itemView.context, R.color.dark_yellow__1)
                )

                cardHeader.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.light_yellow__1)
                )
            }


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