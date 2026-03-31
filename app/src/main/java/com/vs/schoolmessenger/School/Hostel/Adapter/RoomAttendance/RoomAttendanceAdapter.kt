package com.vs.schoolmessenger.School.Hostel.Adapter.RoomAttendance


import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat

import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Hostel.Listner.RoomAttendanceListener
import com.vs.schoolmessenger.School.Hostel.Model.RoomAttendance.HostelRoomAttendanceStudentList.RoomStudentAttendanceData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil
import kotlin.text.equals

class RoomAttendanceAdapter(
    private var itemList: List<RoomStudentAttendanceData>?,
    private var context: Context,
    private var listener: RoomAttendanceListener,
    private var isLoading: Boolean

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.room_attendance_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.room_attendance_item, parent, false)
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

    fun updateData(newList: List<RoomStudentAttendanceData>) {
        itemList = newList
        notifyDataSetChanged()
    }

    fun getUpdatedList(): List<RoomStudentAttendanceData> {
        return itemList ?: emptyList()
    }


    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblInitialName: TextView = itemView.findViewById(R.id.lblInitialName)
        private val lblStudentDetails: TextView = itemView.findViewById(R.id.lblStudentDetails)
        private val lblFullName: TextView = itemView.findViewById(R.id.lblFullName)
        private val lblPresent: MaterialButton = itemView.findViewById(R.id.lblPresent)
        private val lblAbsent: MaterialButton = itemView.findViewById(R.id.lblAbsent)
        private val lblAccept: MaterialButton = itemView.findViewById(R.id.lblAccept)
        private val lblDecline: MaterialButton = itemView.findViewById(R.id.lblDecline)
        private val lblOutpassReason: TextView = itemView.findViewById(R.id.lblOutpassReason)
        private val lblOutpassPermissionFromTimeDuration: TextView =
            itemView.findViewById(R.id.lblOutpassPermissionFromTimeDuration)

        private val lblOutpassPermissionToTimeDuration: TextView =
            itemView.findViewById(R.id.lblOutpassPermissionToTimeDuration)
        private val groupsEntireOutPass: Group = itemView.findViewById(R.id.groupsEntireOutPass)
        private val lblOutpassStatus: TextView = itemView.findViewById(R.id.lblOutpassStatus)
        private val lblParentMobile: TextView = itemView.findViewById(R.id.lblParentMobile)

        private val colorList = listOf(
            R.color.green,
            R.color.dark_blue_color,
            R.color.red,
            R.color.dark_voilet_2,
            R.color.orange,
            R.color.yellow,
            R.color.bpDarker_red,
            R.color.dark_brown,
            R.color.pink_color,
            R.color.dark_green_2,
            R.color.teacher_clr_grey_dark
        )

        fun bind(data: RoomStudentAttendanceData, position: Int) {

            lblInitialName.text = Constant.getInitials(data.name)
            lblStudentDetails.text = "Class : ${data.class_name} - Section : ${data.section_name}"
            lblFullName.text = data.name
            lblParentMobile.text ="Parent Mobile No : ${data.primary_mobile}"

            val color = if (position < colorList.size) {
                colorList[position]
            } else {
                colorList.random()
            }

            lblDecline.backgroundTintList = ContextCompat.getColorStateList(context, R.color.red)
            lblDecline.setTextColor(ContextCompat.getColor(context, R.color.white))
            lblDecline.iconTint = ContextCompat.getColorStateList(context, R.color.white)


            lblAccept.backgroundTintList =
                ContextCompat.getColorStateList(context, R.color.light_green4)
            lblAccept.setTextColor(ContextCompat.getColor(context, R.color.white))
            lblAccept.iconTint = ContextCompat.getColorStateList(context, R.color.white)

            val drawable = lblInitialName.background as GradientDrawable
            drawable.setColor(ContextCompat.getColor(context, color))
            lblInitialName.setTextColor(ContextCompat.getColor(context, R.color.white))

            updateButtonUI(data.status)

            lblPresent.setOnClickListener {
                data.status = "PRESENT"
                updateButtonUI("PRESENT")
                listener.onAttendanceChanged(itemList ?: emptyList())

            }

            lblAbsent.setOnClickListener {
                data.status = "ABSENT"
                updateButtonUI("ABSENT")
                listener.onAttendanceChanged(itemList ?: emptyList())
            }

            val hasOutpassData =
                !data.outpass_id.isNullOrEmpty() ||
                        !data.out_date.isNullOrEmpty() ||
                        !data.in_date.isNullOrEmpty() ||
                        !data.reason.isNullOrEmpty() ||
                        !data.outpasss_status.isNullOrEmpty()



            if (hasOutpassData) {

                groupsEntireOutPass.visibility = View.VISIBLE

                when (data.outpasss_status?.uppercase()) {

                    Constant.approved.uppercase() -> {
                        Log.d("isComig,","isApprove")

                        lblAccept.visibility = View.GONE
                        lblDecline.visibility = View.GONE

                        lblOutpassStatus.background = ContextCompat.getDrawable(
                            context,
                            R.drawable.rect_bg_light_green_approved
                        )
                        lblOutpassStatus.text = "Approved"
                        lblOutpassStatus.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.green
                            )
                        )
                    }

                    Constant.rejected_.uppercase() -> {

                        Log.d("isComig,","isRejected")

                        lblAccept.visibility = View.GONE
                        lblDecline.visibility = View.GONE

                        lblOutpassStatus.background = ContextCompat.getDrawable(
                            context,
                            R.drawable.rect_bg_light_red_rejected
                        )
                        lblOutpassStatus.text = "Rejected"
                        lblOutpassStatus.setTextColor(ContextCompat.getColor(context, R.color.red))
                    }

                    else -> {

                        Log.d("isComig,","isPending")

                        //  Pending case
                        lblAccept.visibility = View.VISIBLE
                        lblDecline.visibility = View.VISIBLE

                        lblOutpassStatus.background = ContextCompat.getDrawable(
                            context,
                            R.drawable.rect_bg_light_orange_pending
                        )
                        lblOutpassStatus.text = "Pending"
                        lblOutpassStatus.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.dark_brown_3
                            )
                        )


                        lblAccept.setOnClickListener {
                            listener.onApproveClicked(data, position, true) { isApproved ->
                                if (isApproved) {
                                    data.outpasss_status = Constant.approved.uppercase()
                                    notifyItemChanged(position)
                                }
                            }
                        }

                        lblDecline.setOnClickListener {
                            listener.onApproveClicked(data, position, false) { isApproved ->
                                if (isApproved) {
                                    data.outpasss_status = Constant.rejected_.uppercase()
                                    notifyItemChanged(position)
                                }
                            }
                        }

                    }
                }


                //  Set common data
                lblOutpassReason.text = "Reason : ${data.reason}"

                lblOutpassPermissionFromTimeDuration.text =
                    "In Date : ${Constant.convertDateTimeFormat2(data.in_date)}"
                lblOutpassPermissionToTimeDuration.text =
                    "Out Date : ${
                        Constant.convertDateTimeFormat2(
                            data.out_date
                        )
                    }"

            } else {
                // No outpass data → hide everything
                groupsEntireOutPass.visibility = View.GONE
                lblAccept.visibility = View.GONE
                lblDecline.visibility = View.GONE
            }


        }

        fun applyTintedBackground(view: View, drawableRes: Int, colorRes: Int) {
            val context = view.context
            val bgDrawable = ContextCompat.getDrawable(context, drawableRes)
            bgDrawable?.setTint(ContextCompat.getColor(context, colorRes))
            view.background = bgDrawable
        }

        private fun updateButtonUI(status: String) {

            if (status.equals("Present", true)) {

                lblPresent.backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.light_green4)
                lblPresent.setTextColor(ContextCompat.getColor(context, R.color.white))
                lblPresent.iconTint =
                    ContextCompat.getColorStateList(context, R.color.white)

                lblAbsent.backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.white)
                lblAbsent.setTextColor(ContextCompat.getColor(context, R.color.black))
                lblAbsent.iconTint =
                    ContextCompat.getColorStateList(context, R.color.black)

            } else if (status.equals("Absent", true)) {

                lblAbsent.backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.red)
                lblAbsent.setTextColor(ContextCompat.getColor(context, R.color.white))
                lblAbsent.iconTint =
                    ContextCompat.getColorStateList(context, R.color.white)

                lblPresent.backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.white)
                lblPresent.setTextColor(ContextCompat.getColor(context, R.color.black))
                lblPresent.iconTint =
                    ContextCompat.getColorStateList(context, R.color.black)

            } else {
                lblAbsent.backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.white)
                lblAbsent.setTextColor(ContextCompat.getColor(context, R.color.black))
                lblAbsent.iconTint =
                    ContextCompat.getColorStateList(context, R.color.black)

                lblPresent.backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.white)
                lblPresent.setTextColor(ContextCompat.getColor(context, R.color.black))
                lblPresent.iconTint =
                    ContextCompat.getColorStateList(context, R.color.black)
            }

            // Apply stroke
            lblPresent.strokeWidth = 2
            lblPresent.strokeColor =
                ContextCompat.getColorStateList(context, R.color.light_gray_10)

            lblAbsent.strokeWidth = 2
            lblAbsent.strokeColor =
                ContextCompat.getColorStateList(context, R.color.light_gray_10)
        }


    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)

        }
    }

}