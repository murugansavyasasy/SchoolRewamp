package com.vs.schoolmessenger.School.Hostel.Adapter.AdminRequests


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Hostel.Model.AdminRequest.AdminRequestWiseData

import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class AdminRequestWise(
    private var itemList: List<AdminRequestWiseData>,
    private val context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var fullList: List<AdminRequestWiseData> = itemList ?: listOf()
    private var filteredList: List<AdminRequestWiseData> = fullList
    private var expandedPosition = RecyclerView.NO_POSITION


    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.admin_request_wise_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.admin_request_wise_item, parent, false)
            DataViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else filteredList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder && !isLoading) {
            val isExpanded = position == expandedPosition
            holder.bind(filteredList[position], context)

        }
    }

    fun updateData(newList: List<AdminRequestWiseData>) {
        fullList = newList
        filteredList = newList
        isLoading = false
        notifyDataSetChanged()
    }


    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblFullRoomNo: TextView = itemView.findViewById(R.id.lblFullRoomNo)
        private val lblRoomNo: TextView = itemView.findViewById(R.id.lblRoomNo)
        private val lblName: TextView = itemView.findViewById(R.id.lblName)
        private val lblReason: TextView = itemView.findViewById(R.id.lblReason)
        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        private val imgStatus: ImageView = itemView.findViewById(R.id.imgStatus)


        @SuppressLint("SetTextI18n")
        fun bind(
            data: AdminRequestWiseData,
            context: Context,
        ) {
            lblFullRoomNo.text = "Room ${data.roomNumber}"
            lblRoomNo.text = data.roomNumber
            lblName.text = data.studentName

            lblReason.text = data.issueDescription
            lblDate.text = data.dateTime


            if (data.status == Constant.rejected) {
                applyTintedBackground(
                    lblRoomNo,
                    R.drawable.rect_bg_light_green_present,
                    R.color.light_red_1
                )
                lblRoomNo.setTextColor(Color.parseColor("#D32F2F"))
                imgStatus.setImageResource(R.drawable.close_red_color)
                imgStatus.setColorFilter(
                    ContextCompat.getColor(itemView.context, R.color.red),
                    PorterDuff.Mode.SRC_IN
                )


            }
            else if (data.status == Constant.approved) {
                applyTintedBackground(
                    lblRoomNo,
                    R.drawable.rect_bg_light_green_present,
                    R.color.light_green_1
                )
                lblRoomNo.setTextColor(Color.parseColor("#2E7D32"))
                imgStatus.setImageResource(R.drawable.tick_icon_2)
                imgStatus.setColorFilter(
                    ContextCompat.getColor(itemView.context, R.color.green),
                    PorterDuff.Mode.SRC_IN
                )


            }
            else if (data.status == Constant.waiting_for_approval) {
                applyTintedBackground(
                    lblRoomNo,
                    R.drawable.rect_bg_light_green_present,
                    R.color.light_green_1
                )
                lblRoomNo.setTextColor(Color.parseColor("#2E7D32"))
                imgStatus.setImageResource(R.drawable.waiting_for_approval)
                imgStatus.setColorFilter(
                    ContextCompat.getColor(itemView.context, R.color.yellow),
                    PorterDuff.Mode.SRC_IN
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
        init {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}