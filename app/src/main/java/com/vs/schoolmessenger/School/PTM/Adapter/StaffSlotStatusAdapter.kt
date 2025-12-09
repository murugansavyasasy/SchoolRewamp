package com.vs.schoolmessenger.School.PTM.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.PTM.DataClass.Slot
import com.vs.schoolmessenger.School.PTM.InterFace.StaffSlotCancelReOpenClickListener
import com.vs.schoolmessenger.Utils.ShimmerUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StaffSlotStatusAdapter(
    private var itemList: List<Slot>? = null,
    private var context: Context,
    private var listener: StaffSlotCancelReOpenClickListener,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_SHIMMER = 0
    private var expandedPosition = -1
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.staff_slot_status_item)
            DataViewHolder.ShimmerViewHolder(
                shimmerView
            )
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.staff_slot_status_item, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            val data = itemList!![position]
            holder.bind(data, position, listener, expandedPosition)

            holder.imgExpand.setOnClickListener {
                expandedPosition = if (expandedPosition == position) -1 else position
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val lblStatus: TextView = itemView.findViewById(R.id.lblStatus)
        private val lblTime: TextView = itemView.findViewById(R.id.lblTime)
        private val lblDuration: TextView = itemView.findViewById(R.id.lblDuration)
        private val lblBookedName: TextView = itemView.findViewById(R.id.lblBookedName)
        private val lblWaitingBooking: TextView = itemView.findViewById(R.id.lblWaitingBooking)
        private val rltStatus: RelativeLayout = itemView.findViewById(R.id.rltStatus)
        private val rltBookedBy: RelativeLayout = itemView.findViewById(R.id.rltBookedBy)
        private val imgStatus: ImageView = itemView.findViewById(R.id.imgStatus)
        private val imgDot: ImageView = itemView.findViewById(R.id.imgDot)
        private val rytSlots: RelativeLayout = itemView.findViewById(R.id.rytSlots)
        private val isConParentName: ConstraintLayout = itemView.findViewById(R.id.conParentName)
        private val txtMotherName: TextView = itemView.findViewById(R.id.txtMotherName)
        private val txtFatherName: TextView = itemView.findViewById(R.id.txtFatherName)
        val imgExpand: ImageView = itemView.findViewById(R.id.imgExpand)
        private val lblStandardAndSection: TextView =
            itemView.findViewById(R.id.lblStandardAndSection)

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(
            data: Slot,
            position: Int,
            listener: StaffSlotCancelReOpenClickListener,
            expandedPos: Int
        ) {
            lblBookedName.text = data.booked_by
            lblStatus.text = data.status
            lblDuration.text = "Duration - ${data.meeting_duration} Minutes"
            lblTime.text = "${data.from_time} - ${data.to_time} (${data.meeting_duration ?: ""} Minutes)"
            lblStandardAndSection.text = "${data.my_class} - ${data.my_section}"


            txtMotherName.text = data.mother_name
            txtFatherName.text = data.father_name

            val isExpanded = position == expandedPos
            isConParentName.visibility = if (isExpanded) View.VISIBLE else View.GONE
            imgExpand.setImageResource(
                if (isExpanded) R.drawable.ic_up_blue_round
                else R.drawable.ic_down_blue_round
            )

            when (data.status) {
                "Available" -> {
                    rltStatus.background = context.getDrawable(R.drawable.bg_light_radious_blue)
                    lblWaitingBooking.visibility = View.VISIBLE
                    lblStatus.setTextColor(context.getColor(R.color.black))
                    imgStatus.setImageDrawable(context.getDrawable(R.drawable.exclamationmark_circle))
                    imgDot.visibility = if (data.can_cancel) View.VISIBLE else View.GONE
                }

                "Cancelled" -> {
                    rltStatus.background = context.getDrawable(R.drawable.bg_light_red_radious)
                    lblWaitingBooking.visibility = View.VISIBLE
                    lblWaitingBooking.text = "Slot Cancelled"
                    lblStatus.setTextColor(context.getColor(R.color.red))
                    lblWaitingBooking.setTextColor(context.getColor(R.color.red))
                    lblWaitingBooking.background = context.getDrawable(R.drawable.bg_light_red_radious)
                    imgStatus.setImageDrawable(context.getDrawable(R.drawable.red_close_icon_))
                    imgDot.visibility = if (data.can_cancel) View.VISIBLE else View.GONE
                }

                "Expired" -> {
                    rltStatus.background = context.getDrawable(R.drawable.gray_bg_raidus_2)
                    lblWaitingBooking.visibility = View.VISIBLE
                    lblWaitingBooking.text = "Slot Expired"
                    lblStatus.setTextColor(context.getColor(R.color.gnt_gray))
                    lblWaitingBooking.setTextColor(context.getColor(R.color.black))
                    lblWaitingBooking.background = context.getDrawable(R.drawable.gray_bg_radius)
                    imgStatus.setImageDrawable(context.getDrawable(R.drawable.expired))
                    imgDot.visibility = View.GONE
                }

                "Completed" -> {
                    lblWaitingBooking.visibility = View.GONE
                    rltStatus.background = context.getDrawable(R.drawable.rect_bg_light_green_radius)
                    imgStatus.setImageDrawable(context.getDrawable(R.drawable.checkmark_circle_icon))
                    imgDot.visibility = View.GONE
                    lblStatus.setTextColor(context.getColor(R.color.dark_green_2))
                    rltBookedBy.visibility = View.VISIBLE
                    lblBookedName.visibility = View.VISIBLE
                    lblBookedName.text = data.booked_by
                }

                "Booked" -> {
                    lblWaitingBooking.visibility = View.GONE
                    rltStatus.background =
                        context.getDrawable(R.drawable.rect_bg_light_green_radius)
                    lblBookedName.visibility = View.VISIBLE
                    rltBookedBy.visibility = View.VISIBLE
                    lblStatus.setTextColor(context.getColor(R.color.dark_green_2))
                    imgStatus.setImageDrawable(context.getDrawable(R.drawable.checkmark_circle_icon))
                    imgDot.visibility = if (data.can_cancel) View.VISIBLE else View.GONE
                }

                "Upcoming" -> {
                    lblWaitingBooking.visibility = View.GONE
                    rltStatus.background =
                        context.getDrawable(R.drawable.rect_bg_light_green_radius)
                    lblStatus.text = "Booked"
                    lblStatus.setTextColor(context.getColor(R.color.dark_green_2))
                    imgStatus.setImageDrawable(context.getDrawable(R.drawable.checkmark_circle_icon))
                    lblBookedName.visibility = View.VISIBLE
                    rltBookedBy.visibility = View.VISIBLE
                    lblBookedName.text = data.booked_by
                    imgDot.visibility = if (data.can_cancel) View.VISIBLE else View.GONE
                    imgDot.visibility = shouldShowImgDot(data.date, data.to_time)
                }
            }
            imgDot.setOnClickListener {
                listener.onStaffSlotCancelReOpenClickListener(data, it, adapterPosition)
            }
        }

        fun shouldShowImgDot(slotDate: String, toTime: String): Int {
            return try {
                val normalizedDate = slotDate.replace("-", "/").trim()
                val normalizedTime = toTime.trim().uppercase(Locale.getDefault())

                val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
                val slotEndDateTime = dateTimeFormat.parse("$normalizedDate $normalizedTime")

                val now = Calendar.getInstance().time

                if (slotEndDateTime != null && now.before(slotEndDateTime)) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                View.GONE
            }
        }

        class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun startShimmer() {
                ShimmerUtil.startShimmer(itemView)
            }
        }
    }
}

