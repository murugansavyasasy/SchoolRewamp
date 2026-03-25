package com.vs.schoolmessenger.School.Hostel.Adapter.RoomAttendance


import android.content.Context
import android.graphics.drawable.GradientDrawable
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
        return itemList?:emptyList()
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
        private val lblOutpassPermissionTimeDuration: TextView = itemView.findViewById(R.id.lblOutpassPermissionTimeDuration)
        private val groupsEntireOutPass: Group = itemView.findViewById(R.id.groupsEntireOutPass)
        private val lblOutpassStatus: TextView = itemView.findViewById(R.id.lblOutpassStatus)

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
            lblStudentDetails.text =
                "Student id :${data.id} Parent Mobile No :${data.primary_mobile}"
            lblFullName.text = data.name

            val color = if (position < colorList.size) {
                colorList[position]
            } else {
                colorList.random()
            }

            val drawable = lblInitialName.background as GradientDrawable
            drawable.setColor(ContextCompat.getColor(context, color))
            lblInitialName.setTextColor(ContextCompat.getColor(context, R.color.white))

            updateButtonUI(data.status)

            lblPresent.setOnClickListener {
                data.status = "PRESENT"
                updateButtonUI("PRESENT")
                listener.onAttendanceChanged(itemList?:emptyList())

            }

            lblAbsent.setOnClickListener {
                data.status = "ABSENT"
                updateButtonUI("ABSENT")
                listener.onAttendanceChanged(itemList?:emptyList())
            }

            when(data.outpass_status){
                Constant.approved.uppercase() -> {
                    groupsEntireOutPass.visibility= View.VISIBLE
                    lblAccept.visibility= View.GONE
                    lblDecline.visibility= View.GONE

                    applyTintedBackground(
                        lblOutpassStatus,
                        R.drawable.green_bg_radius,
                        R.color.very_light_green_3
                    )
                    lblOutpassStatus.text=data.status
                    lblOutpassStatus.setTextColor(context.getColor(R.color.green))



                }
                Constant.rejected_.uppercase()->{
                    groupsEntireOutPass.visibility= View.VISIBLE
                    lblAccept.visibility= View.GONE
                    lblDecline.visibility= View.GONE

                    applyTintedBackground(
                        lblOutpassStatus,
                        R.drawable.green_bg_radius,
                        R.color.very_light_red_3
                    )

                    lblOutpassStatus.text=data.status
                    lblOutpassStatus.setTextColor(context.getColor(R.color.red))


                }

                Constant.pending.uppercase()->{

                    lblAccept.setOnClickListener {
                        listener.onApproveClicked(data, position, true) { isApproved ->
                            if (isApproved) {
                                data.outpass_status = Constant.approved.uppercase()
                            }
                        }
                    }

                    lblDecline.setOnClickListener {
                        listener.onApproveClicked(data, position, false) { isApproved ->
                            if (isApproved) {
                                data.outpass_status = Constant.rejected_.uppercase()

                            }
                        }
                    }

                    lblAccept.backgroundTintList =
                        ContextCompat.getColorStateList(context, R.color.light_green4)
                    lblDecline.backgroundTintList =
                        ContextCompat.getColorStateList(context, R.color.red)

                    groupsEntireOutPass.visibility= View.VISIBLE
                    lblAccept.visibility= View.GONE
                    lblDecline.visibility= View.GONE

                    lblOutpassStatus.text=context.getString(R.string.pending)
                    lblOutpassStatus.setTextColor(context.getColor(R.color.dark_brown_3))
                    applyTintedBackground(
                        lblOutpassStatus,
                        R.drawable.green_bg_radius,
                        R.color.very_light_orange_3
                    )

                    lblOutpassReason.text=data.reason
                    lblOutpassPermissionTimeDuration.text= "In Date : ${Constant.formatDateTime(data.in_date)} - Out Date : ${
                        Constant.formatDateTime(data.out_date)}"
                }
                else -> {
                    groupsEntireOutPass.visibility= View.GONE
                }


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

            }
            else if(status.equals("Absent", true)) {

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

            }

            else {
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