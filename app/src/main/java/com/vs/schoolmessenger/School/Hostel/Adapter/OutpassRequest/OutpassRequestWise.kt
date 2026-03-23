package com.vs.schoolmessenger.School.Hostel.Adapter.OutpassRequest




import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Hostel.Model.AdminRequest.AdminRequestWiseData
import com.vs.schoolmessenger.School.Hostel.Model.OutPassRequest.OutpassRequestWiseData
import com.vs.schoolmessenger.School.Hostel.Model.OutPassRequest.StatusWiseOutpassRequestData

import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class OutpassRequestWise(
    private var itemList: List<OutpassRequestWiseData>,
    private val context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var fullList: List<OutpassRequestWiseData> = itemList ?: listOf()
    private var filteredList: List<OutpassRequestWiseData> = fullList


    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.outpass_request_wise_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.outpass_request_wise_item, parent, false)
            DataViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else filteredList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder && !isLoading) {
            holder.bind(filteredList[position], context)

        }
    }

    fun updateData(newList: List<OutpassRequestWiseData>) {
        fullList = newList
        filteredList = newList
        isLoading = false
        notifyDataSetChanged()
    }


    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblRoomFullNo: TextView = itemView.findViewById(R.id.lblRoomFullNo)
        private val lblInitialName: TextView = itemView.findViewById(R.id.lblInitialName)
        private val lblFullName: TextView = itemView.findViewById(R.id.lblFullName)
        private val lblDestination: TextView = itemView.findViewById(R.id.lblDestination)
        private val lblOutDate: TextView = itemView.findViewById(R.id.lblOutDate)
        private val lblInDate: TextView = itemView.findViewById(R.id.lblInDate)
        private val lblReason: TextView = itemView.findViewById(R.id.lblReason)
        private val lblStatus: TextView = itemView.findViewById(R.id.lblStatus)
        private val lblApprove: MaterialButton = itemView.findViewById(R.id.lblApprove)
        private val lblRejected: MaterialButton = itemView.findViewById(R.id.lblRejected)
        private val groupApproveReject: Group = itemView.findViewById(R.id.groupApproveReject)

        private val cardHeader: MaterialCardView = itemView.findViewById(R.id.cardHeader)


        @SuppressLint("SetTextI18n")
        fun bind(
            data: OutpassRequestWiseData,
            context: Context,
        ) {

            lblRoomFullNo.text = "Room ${data.room_no}"
            lblFullName.text = data.student_name
            lblInitialName.text= Constant.getInitials(data.student_name?:"")

            lblReason.text = data.reason
            lblInDate.text =  "In : ${data.in_date}"
            lblOutDate.text = "Out : ${data.out_date}"
            lblDestination.visibility= View.GONE

            lblApprove.setBackgroundTintList(
                ContextCompat.getColorStateList(context, R.color.light_green4)
            )

            lblRejected.setBackgroundTintList(
                ContextCompat.getColorStateList(context, R.color.red)
            )

            if (data.status == Constant.rejected) {
                groupApproveReject.visibility= View.GONE

                applyTintedBackground(
                    lblInitialName,
                    R.drawable.circle_bg_orange,
                    R.color.red
                )

                cardHeader.setStrokeColor(
                    ContextCompat.getColor(itemView.context, R.color.light_red_1)
                )
                applyTintedBackground(
                    lblStatus,
                    R.drawable.rect_bg_light_green_present,
                    R.color.very_light_red_3
                )

                lblStatus.text=data.status
                lblStatus.setTextColor(context.getColor(R.color.red))

                cardHeader.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.light_red)
                )




            }
            else if (data.status == Constant.approved) {

                groupApproveReject.visibility= View.GONE
                applyTintedBackground(
                    lblInitialName,
                    R.drawable.circle_bg_orange,
                    R.color.green
                )
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
            else if (data.status == Constant.waiting_for_approval) {
                groupApproveReject.visibility= View.VISIBLE

                applyTintedBackground(
                    lblInitialName,
                    R.drawable.circle_bg_orange,
                    R.color.dark_orange_3
                )
                lblStatus.text=context.getString(R.string.pending)
                lblStatus.setTextColor(context.getColor(R.color.dark_brown_3))



                applyTintedBackground(
                    lblStatus,
                    R.drawable.rect_bg_light_green_present,
                    R.color.very_light_orange_3
                )

                cardHeader.setStrokeColor(
                    ContextCompat.getColor(itemView.context, R.color.light_orange_four)
                )

                cardHeader.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.light_orange_4)
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