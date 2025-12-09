package com.vs.schoolmessenger.School.PTM.Adapter

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.imageview.ShapeableImageView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.PTM.DataClass.BookedSlotItem
import com.vs.schoolmessenger.School.PTM.InterFace.BookedSlotCancelReOpenClickListener
import com.vs.schoolmessenger.Utils.Constant

class BookedSlotAdapter(
    private val bookedSlotList: List<BookedSlotItem>,
    private val context: Context,
    private val listener: BookedSlotCancelReOpenClickListener,
    private val isShimmer: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private var expandedPosition: Int = -1

    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int =
        if (isShimmer) TYPE_SHIMMER else TYPE_DATA

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.shimmer_view_big_list, parent, false)
            ShimmerViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_booked_slot, parent, false)
            BookedViewHolder(view)
        }
    }

    override fun getItemCount(): Int = if (isShimmer) 6 else bookedSlotList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ShimmerViewHolder) holder.startShimmer()
        else if (holder is BookedViewHolder) holder.bind(bookedSlotList[position])
    }

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmer: ShimmerFrameLayout = itemView.findViewById(R.id.shimmer_view_container)
        fun startShimmer() = shimmer.startShimmer()
    }

    inner class BookedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        private val lblMode: TextView = itemView.findViewById(R.id.lblMode)
        private val lblTime: TextView = itemView.findViewById(R.id.lblTime)
        private val lblDuration: TextView = itemView.findViewById(R.id.lblDuration)
        private val lblName: TextView = itemView.findViewById(R.id.lblName)
        private val lblClass: TextView = itemView.findViewById(R.id.lblClass)
        private val txtFather: TextView = itemView.findViewById(R.id.txtFatherName)
        private val txtMother: TextView = itemView.findViewById(R.id.txtMotherName)
        private val imgProfile: ShapeableImageView = itemView.findViewById(R.id.imgProfile)
        private val imgExpand: ImageView = itemView.findViewById(R.id.imgExpand)
        private val imgDot: ImageView = itemView.findViewById(R.id.imgDot)
        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        private val lblDate2: TextView = itemView.findViewById(R.id.lblDate2)
        private val isConParentName: ConstraintLayout = itemView.findViewById(R.id.conParentName)

        private val txtMotherName: TextView = itemView.findViewById(R.id.txtMotherName)
        private val txtFatherName: TextView = itemView.findViewById(R.id.txtFatherName)
        private val lblStatus: TextView = itemView.findViewById(R.id.lblStatus)
        private val imgCheckmark: ImageView = itemView.findViewById(R.id.imgCheckmark)
        private val rytStatusBanner: RelativeLayout = itemView.findViewById(R.id.rytStatusBanner)
        private val lblWaitingBooking: TextView = itemView.findViewById(R.id.lblWaitingBooking)
        private val cardBookedBy: CardView = itemView.findViewById(R.id.cardBookedBy)
        private val imgMeetingType: ImageView = itemView.findViewById(R.id.imgMeetingType)

        fun bind(data: BookedSlotItem) {
            lblTitle.text = data.event_name ?: ""
            lblDate.text = Constant.covertDate(data.date.toString()) ?: ""
            lblDate2.text = Constant.covertDate(data.date.toString()) ?: ""
            lblMode.text = data.event_mode ?: ""
            lblTime.text = "${data.from_time ?: ""} - ${data.to_time ?: ""} (${data.meeting_duration ?: ""} Minutes)"
            lblDuration.text = "Duration - ${data.meeting_duration ?: ""} Minutes"
            lblName.text = data.student_name ?: ""
            lblClass.text = "${data.class_name ?: ""} - ${data.section_name ?: ""}"
            txtFather.text = data.father_name ?: ""
            txtMother.text = data.mother_name ?: ""

            when (data.event_mode) {
                "Virtual","virtual", "online", "video call", "zoom" -> {
                    imgMeetingType.setImageResource(R.drawable.network)
                    imgMeetingType.setColorFilter(ContextCompat.getColor(context, R.color.black), PorterDuff.Mode.SRC_IN)
                }
                "In Person","in person", "in-person", "person" -> {
                    imgMeetingType.setImageResource(R.drawable.location_simple_icon)
                    imgMeetingType.setColorFilter(ContextCompat.getColor(context, R.color.black), PorterDuff.Mode.SRC_IN)
                }
                "Phone Call","phone call", "call", "phone" -> {
                    imgMeetingType.setImageResource(R.drawable.phone_icon_2)
                    imgMeetingType.setColorFilter(ContextCompat.getColor(context, R.color.black), PorterDuff.Mode.SRC_IN)
                }
            }


            val position = adapterPosition
            val isExpanded = position == expandedPosition
            isConParentName.visibility = if (isExpanded) View.VISIBLE else View.GONE
            imgExpand.setImageResource(
                if (isExpanded) R.drawable.ic_up_blue_round else R.drawable.ic_down_blue_round
            )
            imgExpand.setOnClickListener {
                expandedPosition = if (isExpanded) {
                    -1
                } else {
                    position
                }
                notifyDataSetChanged()
            }

            if (data.can_cancel!!){
                if (data.slot_status=="Completed"){
                    imgDot.visibility= View.GONE
                }else{
                    imgDot.visibility= View.VISIBLE
                }
            }else{
                imgDot.visibility= View.GONE
            }
            if (data.is_cancelled_by_staff!!) {
                lblWaitingBooking.visibility = View.VISIBLE
                rytStatusBanner.background = context.getDrawable(R.drawable.bg_light_red_radious)
                cardBookedBy.visibility = View.GONE
                lblStatus.setTextColor(context.getColor(R.color.red))
                lblStatus.text = "Canceled"
                lblWaitingBooking.setTextColor(context.getColor(R.color.red))
                lblWaitingBooking.text = "Slot Canceled"
                imgCheckmark.setImageDrawable(context.getDrawable(R.drawable.red_close_icon_))
                lblWaitingBooking.background = context.getDrawable(R.drawable.bg_light_red_radious)
            } else {
                if (data.slot_status == "Available") {
                    lblWaitingBooking.visibility = View.GONE
                    cardBookedBy.visibility = View.VISIBLE
                    lblStatus.setTextColor(context.getColor(R.color.Blue))
                    rytStatusBanner.background = context.getDrawable(R.drawable.rect_bg_light_green_radius)
                    imgCheckmark.setImageDrawable(context.getDrawable(R.drawable.checkmark_circle_icon))

                } else if (data.slot_status == "Cancelled") {
                    lblWaitingBooking.visibility = View.VISIBLE
                    cardBookedBy.visibility = View.GONE
                    rytStatusBanner.background = context.getDrawable(R.drawable.bg_light_red_radious)
                    lblWaitingBooking.text = "Slot Cancelled"
                    lblStatus.setTextColor(context.getColor(R.color.red))
                    lblWaitingBooking.setTextColor(context.getColor(R.color.red))
                    imgCheckmark.setImageDrawable(context.getDrawable(R.drawable.red_close_icon_))
                    lblWaitingBooking.background = context.getDrawable(R.drawable.bg_light_red_radious)
                } else if (data.slot_status == "Expired") {
                    lblWaitingBooking.visibility = View.VISIBLE
                    cardBookedBy.visibility = View.GONE
                    rytStatusBanner.background = context.getDrawable(R.drawable.gray_bg_raidus_2)
                    lblWaitingBooking.text = "Slot Expired"
                    lblWaitingBooking.setTextColor(context.getColor(R.color.black))
                    lblStatus.setTextColor(context.getColor(R.color.gnt_gray))
                    lblWaitingBooking.background = context.getDrawable(R.drawable.gray_bg_radius)
                    imgCheckmark.setImageDrawable(context.getDrawable(R.drawable.expired))
                } else if (data.slot_status == "Completed") {
                    lblWaitingBooking.visibility = View.GONE
                    rytStatusBanner.background = context.getDrawable(R.drawable.rect_bg_light_green_radius)
                    cardBookedBy.visibility = View.VISIBLE
                    lblStatus.setTextColor(context.getColor(R.color.dark_green_2))
                    imgCheckmark.setImageDrawable(context.getDrawable(R.drawable.checkmark_circle_icon))
                } else if (data.slot_status == "Booked") {
                    rytStatusBanner.background = context.getDrawable(R.drawable.rect_bg_light_green_radius)
                    lblWaitingBooking.visibility = View.GONE
                    cardBookedBy.visibility = View.VISIBLE
                    lblStatus.setTextColor(context.getColor(R.color.dark_green_2))
                    imgCheckmark.setImageDrawable(context.getDrawable(R.drawable.checkmark_circle_icon))
                } else if (data.slot_status == "Upcoming") {
                    lblWaitingBooking.visibility = View.GONE
                    rytStatusBanner.background = context.getDrawable(R.drawable.rect_bg_light_green_radius)
                    cardBookedBy.visibility = View.VISIBLE
                    lblStatus.setTextColor(context.getColor(R.color.dark_green_2))
                    lblStatus.text = "Booked"
                    imgCheckmark.setImageDrawable(context.getDrawable(R.drawable.checkmark_circle_icon))
                }
            }

            imgDot.setOnClickListener {
                listener.onBookedSlotCancelReOpenClickListener(
                    data,
                    it,
                    adapterPosition
                )
            }

            Glide.with(itemView.context)
                .load(data.profile_url)
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .into(imgProfile)
        }
    }
}